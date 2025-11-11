package io.github.mvrao94.kubeguard.service;

import io.github.mvrao94.kubeguard.model.*;
import io.github.mvrao94.kubeguard.repository.ScanReportRepository;
import io.github.mvrao94.kubeguard.service.rules.SecurityRulesEngine;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.Config;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yaml.snakeyaml.Yaml;

/** Service for performing security scans on Kubernetes resources */
@Service
public class ScanService {

  private static final Logger logger = LoggerFactory.getLogger(ScanService.class);

  @Autowired private ScanReportRepository scanReportRepository;

  @Autowired private SecurityRulesEngine rulesEngine;

  private CoreV1Api coreV1Api;
  private AppsV1Api appsV1Api;
  private io.kubernetes.client.openapi.apis.NetworkingV1Api networkingV1Api;
  private io.kubernetes.client.openapi.apis.RbacAuthorizationV1Api rbacV1Api;

  public ScanService() {
    try {
      ApiClient client = Config.defaultClient();
      Configuration.setDefaultApiClient(client);
      this.coreV1Api = new CoreV1Api();
      this.appsV1Api = new AppsV1Api();
      this.networkingV1Api = new io.kubernetes.client.openapi.apis.NetworkingV1Api();
      this.rbacV1Api = new io.kubernetes.client.openapi.apis.RbacAuthorizationV1Api();
    } catch (IOException e) {
      logger.warn("Failed to initialize Kubernetes client: {}", e.getMessage());
    }
  }

  /** Scan Kubernetes manifest files in a directory */
  @Async("scanExecutor")
  @Transactional
  public CompletableFuture<ScanReport> scanManifests(String directoryPath, String scanId) {
    logger.info("Starting manifest scan for directory: {} with scanId: {}", directoryPath, scanId);

    ScanReport report = new ScanReport(scanId, ScanType.MANIFEST, directoryPath);
    report = scanReportRepository.save(report);

    try {
      List<SecurityFinding> allFindings = new ArrayList<>();
      int totalResources = 0;

      // Validate and sanitize the path to prevent path traversal attacks
      Path dir = Paths.get(directoryPath).normalize().toAbsolutePath();
      
      // Prevent path traversal by checking for suspicious patterns
      if (directoryPath.contains("..") || directoryPath.contains("~")) {
        throw new IllegalArgumentException("Invalid directory path: path traversal not allowed");
      }
      
      if (!Files.exists(dir) || !Files.isDirectory(dir)) {
        throw new IllegalArgumentException("Directory does not exist: " + directoryPath);
      }
      
      // Additional security check: ensure the path is readable
      if (!Files.isReadable(dir)) {
        throw new IllegalArgumentException("Directory is not readable: " + directoryPath);
      }

      try (Stream<Path> paths = Files.walk(dir)) {
        List<Path> yamlFiles =
            paths
                .filter(Files::isRegularFile)
                .filter(
                    path -> path.toString().endsWith(".yaml") || path.toString().endsWith(".yml"))
                .toList();

        for (Path yamlFile : yamlFiles) {
          logger.debug("Processing file: {}", yamlFile);
          List<SecurityFinding> fileFindings = scanYamlFile(yamlFile.toFile(), report);
          allFindings.addAll(fileFindings);
          totalResources++;
        }
      }

      // Update report with findings
      updateReportWithFindings(report, allFindings, totalResources);
      report.setStatus(ScanStatus.COMPLETED);

    } catch (Exception e) {
      logger.error("Error during manifest scan: ", e);
      report.setStatus(ScanStatus.FAILED);
      report.setErrorMessage(e.getMessage());
    }

    scanReportRepository.save(report);
    logger.info("Completed manifest scan with scanId: {}", scanId);
    return CompletableFuture.completedFuture(report);
  }

  /** Scan live Kubernetes cluster resources in a namespace */
  @Async("scanExecutor")
  @Transactional
  public CompletableFuture<ScanReport> scanCluster(String namespace, String scanId) {
    logger.info("Starting cluster scan for namespace: {} with scanId: {}", namespace, scanId);

    ScanReport report = new ScanReport(scanId, ScanType.CLUSTER, namespace);
    report = scanReportRepository.save(report);

    if (coreV1Api == null || appsV1Api == null) {
      report.setStatus(ScanStatus.FAILED);
      report.setErrorMessage("Kubernetes client not properly initialized");
      scanReportRepository.save(report);
      return CompletableFuture.completedFuture(report);
    }

    try {
      List<SecurityFinding> allFindings = new ArrayList<>();
      int totalResources = 0;

      // Scan Deployments
      V1DeploymentList deployments = appsV1Api.listNamespacedDeployment(namespace).execute();
      for (V1Deployment deployment : deployments.getItems()) {
        List<SecurityFinding> findings = rulesEngine.analyzeDeployment(deployment);
        for (SecurityFinding finding : findings) {
          finding.setScanReport(report);
          allFindings.add(finding);
        }
        totalResources++;
      }

      // Scan Pods
      V1PodList pods = coreV1Api.listNamespacedPod(namespace).execute();
      for (V1Pod pod : pods.getItems()) {
        List<SecurityFinding> findings = rulesEngine.analyzePod(pod);
        for (SecurityFinding finding : findings) {
          finding.setScanReport(report);
          allFindings.add(finding);
        }
        totalResources++;
      }

      // Scan Services
      V1ServiceList services = coreV1Api.listNamespacedService(namespace).execute();
      for (V1Service service : services.getItems()) {
        List<SecurityFinding> findings = rulesEngine.analyzeService(service);
        for (SecurityFinding finding : findings) {
          finding.setScanReport(report);
          allFindings.add(finding);
        }
        totalResources++;
      }

      // Scan for service account usage in pods
      for (V1Pod pod : pods.getItems()) {
        List<SecurityFinding> findings =
            rulesEngine.analyzePodServiceAccount(
                pod.getSpec(),
                pod.getMetadata().getName(),
                "Pod",
                pod.getMetadata().getNamespace());
        for (SecurityFinding finding : findings) {
          finding.setScanReport(report);
          allFindings.add(finding);
        }
      }

      // Scan NetworkPolicies
      if (networkingV1Api != null) {
        try {
          io.kubernetes.client.openapi.models.V1NetworkPolicyList networkPolicies =
              networkingV1Api.listNamespacedNetworkPolicy(namespace).execute();
          List<SecurityFinding> findings =
              rulesEngine.analyzeNetworkPolicies(networkPolicies.getItems(), namespace);
          for (SecurityFinding finding : findings) {
            finding.setScanReport(report);
            allFindings.add(finding);
          }
          totalResources += networkPolicies.getItems().size();
        } catch (ApiException e) {
          logger.warn("Failed to scan NetworkPolicies: {}", e.getMessage());
        }
      }

      // Scan Ingresses
      if (networkingV1Api != null) {
        try {
          io.kubernetes.client.openapi.models.V1IngressList ingresses =
              networkingV1Api.listNamespacedIngress(namespace).execute();
          for (io.kubernetes.client.openapi.models.V1Ingress ingress : ingresses.getItems()) {
            List<SecurityFinding> findings = rulesEngine.analyzeIngress(ingress);
            for (SecurityFinding finding : findings) {
              finding.setScanReport(report);
              allFindings.add(finding);
            }
            totalResources++;
          }
        } catch (ApiException e) {
          logger.warn("Failed to scan Ingresses: {}", e.getMessage());
        }
      }

      // Scan Roles
      if (rbacV1Api != null) {
        try {
          io.kubernetes.client.openapi.models.V1RoleList roles =
              rbacV1Api.listNamespacedRole(namespace).execute();
          for (io.kubernetes.client.openapi.models.V1Role role : roles.getItems()) {
            List<SecurityFinding> findings = rulesEngine.analyzeRole(role);
            for (SecurityFinding finding : findings) {
              finding.setScanReport(report);
              allFindings.add(finding);
            }
            totalResources++;
          }
        } catch (ApiException e) {
          logger.warn("Failed to scan Roles: {}", e.getMessage());
        }
      }

      // Scan ClusterRoles (cluster-wide, but we'll associate with this scan)
      if (rbacV1Api != null) {
        try {
          io.kubernetes.client.openapi.models.V1ClusterRoleList clusterRoles =
              rbacV1Api.listClusterRole().execute();
          for (io.kubernetes.client.openapi.models.V1ClusterRole clusterRole :
              clusterRoles.getItems()) {
            List<SecurityFinding> findings = rulesEngine.analyzeClusterRole(clusterRole);
            for (SecurityFinding finding : findings) {
              finding.setScanReport(report);
              allFindings.add(finding);
            }
            totalResources++;
          }
        } catch (ApiException e) {
          logger.warn("Failed to scan ClusterRoles: {}", e.getMessage());
        }
      }

      // Update report with findings
      updateReportWithFindings(report, allFindings, totalResources);
      report.setStatus(ScanStatus.COMPLETED);

    } catch (ApiException e) {
      logger.error("Kubernetes API error during cluster scan: ", e);
      report.setStatus(ScanStatus.FAILED);
      report.setErrorMessage("Kubernetes API error: " + e.getResponseBody());
    } catch (Exception e) {
      logger.error("Error during cluster scan: ", e);
      report.setStatus(ScanStatus.FAILED);
      report.setErrorMessage(e.getMessage());
    }

    scanReportRepository.save(report);
    logger.info("Completed cluster scan with scanId: {}", scanId);
    return CompletableFuture.completedFuture(report);
  }

  private List<SecurityFinding> scanYamlFile(File file, ScanReport report) {
    List<SecurityFinding> findings = new ArrayList<>();

    try (FileInputStream fis = new FileInputStream(file)) {
      Yaml yaml = new Yaml();

      // Handle multiple documents in one YAML file
      Iterable<Object> documents = yaml.loadAll(fis);

      for (Object document : documents) {
        if (document instanceof Map) {
          @SuppressWarnings("unchecked")
          Map<String, Object> resourceMap = (Map<String, Object>) document;
          findings.addAll(analyzeYamlResource(resourceMap, file.getName(), report));
        }
      }

    } catch (Exception e) {
      logger.error("Error processing YAML file {}: ", file.getName(), e);

      // Create a finding for the parsing error
      SecurityFinding errorFinding = new SecurityFinding();
      errorFinding.setScanReport(report);
      errorFinding.setResourceName(file.getName());
      errorFinding.setResourceType("File");
      errorFinding.setRuleId("PARSE001");
      errorFinding.setTitle("YAML Parsing Error");
      errorFinding.setDescription("Failed to parse YAML file: " + e.getMessage());
      errorFinding.setSeverity(Severity.HIGH);
      errorFinding.setCategory("Configuration");
      errorFinding.setRemediation("Fix YAML syntax errors in the file.");
      errorFinding.setLocation(file.getPath());
      findings.add(errorFinding);
    }

    return findings;
  }

  private List<SecurityFinding> analyzeYamlResource(
      Map<String, Object> resource, String fileName, ScanReport report) {
    List<SecurityFinding> findings = new ArrayList<>();

    String kind = (String) resource.get("kind");
    if (kind == null) {
      return findings;
    }

    try {
      switch (kind) {
        case "Deployment":
          V1Deployment deployment = parseDeployment(resource);
          if (deployment != null) {
            List<SecurityFinding> deploymentFindings = rulesEngine.analyzeDeployment(deployment);
            deploymentFindings.forEach(
                finding -> {
                  finding.setScanReport(report);
                  finding.setLocation(fileName);
                });
            findings.addAll(deploymentFindings);
          }
          break;

        case "Pod":
          V1Pod pod = parsePod(resource);
          if (pod != null) {
            List<SecurityFinding> podFindings = rulesEngine.analyzePod(pod);
            podFindings.forEach(
                finding -> {
                  finding.setScanReport(report);
                  finding.setLocation(fileName);
                });
            findings.addAll(podFindings);
          }
          break;

        case "Service":
          V1Service service = parseService(resource);
          if (service != null) {
            List<SecurityFinding> serviceFindings = rulesEngine.analyzeService(service);
            serviceFindings.forEach(
                finding -> {
                  finding.setScanReport(report);
                  finding.setLocation(fileName);
                });
            findings.addAll(serviceFindings);
          }
          break;

        case "Ingress":
          io.kubernetes.client.openapi.models.V1Ingress ingress = parseIngress(resource);
          if (ingress != null) {
            List<SecurityFinding> ingressFindings = rulesEngine.analyzeIngress(ingress);
            ingressFindings.forEach(
                finding -> {
                  finding.setScanReport(report);
                  finding.setLocation(fileName);
                });
            findings.addAll(ingressFindings);
          }
          break;

        case "NetworkPolicy":
          io.kubernetes.client.openapi.models.V1NetworkPolicy networkPolicy =
              parseNetworkPolicy(resource);
          if (networkPolicy != null) {
            // NetworkPolicy analysis is done at namespace level, but we can still parse it
            logger.debug("NetworkPolicy found in manifest: {}", fileName);
          }
          break;

        case "Role":
          io.kubernetes.client.openapi.models.V1Role role = parseRole(resource);
          if (role != null) {
            List<SecurityFinding> roleFindings = rulesEngine.analyzeRole(role);
            roleFindings.forEach(
                finding -> {
                  finding.setScanReport(report);
                  finding.setLocation(fileName);
                });
            findings.addAll(roleFindings);
          }
          break;

        case "ClusterRole":
          io.kubernetes.client.openapi.models.V1ClusterRole clusterRole =
              parseClusterRole(resource);
          if (clusterRole != null) {
            List<SecurityFinding> clusterRoleFindings = rulesEngine.analyzeClusterRole(clusterRole);
            clusterRoleFindings.forEach(
                finding -> {
                  finding.setScanReport(report);
                  finding.setLocation(fileName);
                });
            findings.addAll(clusterRoleFindings);
          }
          break;

        default:
          logger.debug("Unsupported resource type for security analysis: {}", kind);
      }
    } catch (Exception e) {
      logger.error("Error analyzing {} resource in file {}: ", kind, fileName, e);
    }

    return findings;
  }

  private V1Deployment parseDeployment(Map<String, Object> resource) {
    // This is a simplified parser - in production, you'd use proper JSON/YAML to object mapping
    try {
      V1Deployment deployment = new V1Deployment();

      // Parse metadata
      @SuppressWarnings("unchecked")
      Map<String, Object> metadata = (Map<String, Object>) resource.get("metadata");
      if (metadata != null) {
        V1ObjectMeta objectMeta = new V1ObjectMeta();
        objectMeta.setName((String) metadata.get("name"));
        objectMeta.setNamespace((String) metadata.get("namespace"));
        deployment.setMetadata(objectMeta);
      }

      // Parse spec - simplified version
      @SuppressWarnings("unchecked")
      Map<String, Object> spec = (Map<String, Object>) resource.get("spec");
      if (spec != null) {
        V1DeploymentSpec deploymentSpec = new V1DeploymentSpec();

        @SuppressWarnings("unchecked")
        Map<String, Object> template = (Map<String, Object>) spec.get("template");
        if (template != null) {
          V1PodTemplateSpec podTemplate = new V1PodTemplateSpec();

          @SuppressWarnings("unchecked")
          Map<String, Object> podSpec = (Map<String, Object>) template.get("spec");
          if (podSpec != null) {
            V1PodSpec podSpecObj = parsePodSpec(podSpec);
            podTemplate.setSpec(podSpecObj);
          }

          deploymentSpec.setTemplate(podTemplate);
        }

        deployment.setSpec(deploymentSpec);
      }

      return deployment;
    } catch (Exception e) {
      logger.error("Error parsing Deployment: ", e);
      return null;
    }
  }

  private V1Pod parsePod(Map<String, Object> resource) {
    try {
      V1Pod pod = new V1Pod();

      // Parse metadata
      @SuppressWarnings("unchecked")
      Map<String, Object> metadata = (Map<String, Object>) resource.get("metadata");
      if (metadata != null) {
        V1ObjectMeta objectMeta = new V1ObjectMeta();
        objectMeta.setName((String) metadata.get("name"));
        objectMeta.setNamespace((String) metadata.get("namespace"));
        pod.setMetadata(objectMeta);
      }

      // Parse spec
      @SuppressWarnings("unchecked")
      Map<String, Object> spec = (Map<String, Object>) resource.get("spec");
      if (spec != null) {
        V1PodSpec podSpec = parsePodSpec(spec);
        pod.setSpec(podSpec);
      }

      return pod;
    } catch (Exception e) {
      logger.error("Error parsing Pod: ", e);
      return null;
    }
  }

  private V1Service parseService(Map<String, Object> resource) {
    try {
      V1Service service = new V1Service();

      // Parse metadata
      @SuppressWarnings("unchecked")
      Map<String, Object> metadata = (Map<String, Object>) resource.get("metadata");
      if (metadata != null) {
        V1ObjectMeta objectMeta = new V1ObjectMeta();
        objectMeta.setName((String) metadata.get("name"));
        objectMeta.setNamespace((String) metadata.get("namespace"));
        service.setMetadata(objectMeta);
      }

      // Parse spec
      @SuppressWarnings("unchecked")
      Map<String, Object> spec = (Map<String, Object>) resource.get("spec");
      if (spec != null) {
        V1ServiceSpec serviceSpec = new V1ServiceSpec();
        serviceSpec.setType((String) spec.get("type"));
        service.setSpec(serviceSpec);
      }

      return service;
    } catch (Exception e) {
      logger.error("Error parsing Service: ", e);
      return null;
    }
  }

  private V1PodSpec parsePodSpec(Map<String, Object> spec) {
    V1PodSpec podSpec = new V1PodSpec();

    // Parse containers
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> containers = (List<Map<String, Object>>) spec.get("containers");
    if (containers != null) {
      List<V1Container> containerList = new ArrayList<>();
      for (Map<String, Object> containerMap : containers) {
        V1Container container = parseContainer(containerMap);
        if (container != null) {
          containerList.add(container);
        }
      }
      podSpec.setContainers(containerList);
    }

    // Parse security context
    @SuppressWarnings("unchecked")
    Map<String, Object> securityContext = (Map<String, Object>) spec.get("securityContext");
    if (securityContext != null) {
      V1PodSecurityContext podSecurityContext = parsePodSecurityContext(securityContext);
      podSpec.setSecurityContext(podSecurityContext);
    }

    return podSpec;
  }

  private V1Container parseContainer(Map<String, Object> containerMap) {
    V1Container container = new V1Container();

    container.setName((String) containerMap.get("name"));
    container.setImage((String) containerMap.get("image"));

    // Parse security context
    @SuppressWarnings("unchecked")
    Map<String, Object> securityContext = (Map<String, Object>) containerMap.get("securityContext");
    if (securityContext != null) {
      V1SecurityContext secCtx = parseSecurityContext(securityContext);
      container.setSecurityContext(secCtx);
    }

    // Parse resources
    @SuppressWarnings("unchecked")
    Map<String, Object> resources = (Map<String, Object>) containerMap.get("resources");
    if (resources != null) {
      V1ResourceRequirements resourceReq = parseResourceRequirements(resources);
      container.setResources(resourceReq);
    }

    return container;
  }

  private V1SecurityContext parseSecurityContext(Map<String, Object> securityContext) {
    V1SecurityContext secCtx = new V1SecurityContext();

    if (securityContext.get("privileged") != null) {
      secCtx.setPrivileged((Boolean) securityContext.get("privileged"));
    }

    if (securityContext.get("runAsUser") != null) {
      secCtx.setRunAsUser(Long.valueOf(securityContext.get("runAsUser").toString()));
    }

    if (securityContext.get("readOnlyRootFilesystem") != null) {
      secCtx.setReadOnlyRootFilesystem((Boolean) securityContext.get("readOnlyRootFilesystem"));
    }

    return secCtx;
  }

  private V1PodSecurityContext parsePodSecurityContext(Map<String, Object> securityContext) {
    V1PodSecurityContext podSecCtx = new V1PodSecurityContext();

    if (securityContext.get("runAsUser") != null) {
      podSecCtx.setRunAsUser(Long.valueOf(securityContext.get("runAsUser").toString()));
    }

    if (securityContext.get("fsGroup") != null) {
      podSecCtx.setFsGroup(Long.valueOf(securityContext.get("fsGroup").toString()));
    }

    return podSecCtx;
  }

  private V1ResourceRequirements parseResourceRequirements(Map<String, Object> resources) {
    V1ResourceRequirements resourceReq = new V1ResourceRequirements();

    @SuppressWarnings("unchecked")
    Map<String, Object> limits = (Map<String, Object>) resources.get("limits");

    if (limits != null) {
      Map<String, io.kubernetes.client.custom.Quantity> quantityLimits = new HashMap<>();
      for (Map.Entry<String, Object> entry : limits.entrySet()) {
        quantityLimits.put(
            entry.getKey(),
            io.kubernetes.client.custom.Quantity.fromString(entry.getValue().toString()));
      }
      resourceReq.setLimits(quantityLimits);
    }

    @SuppressWarnings("unchecked")
    Map<String, Object> requests = (Map<String, Object>) resources.get("requests");

    if (requests != null) {
      Map<String, io.kubernetes.client.custom.Quantity> quantityRequests = new HashMap<>();
      for (Map.Entry<String, Object> entry : requests.entrySet()) {
        quantityRequests.put(
            entry.getKey(),
            io.kubernetes.client.custom.Quantity.fromString(entry.getValue().toString()));
      }
      resourceReq.setRequests(quantityRequests);
    }

    return resourceReq;
  }

  private void updateReportWithFindings(
      ScanReport report, List<SecurityFinding> findings, int totalResources) {
    report.setTotalResources(totalResources);

    // Count findings by severity
    long criticalCount =
        findings.stream().filter(f -> f.getSeverity() == Severity.CRITICAL).count();
    long highCount = findings.stream().filter(f -> f.getSeverity() == Severity.HIGH).count();
    long mediumCount = findings.stream().filter(f -> f.getSeverity() == Severity.MEDIUM).count();
    long lowCount = findings.stream().filter(f -> f.getSeverity() == Severity.LOW).count();

    report.setCriticalIssues((int) criticalCount);
    report.setHighIssues((int) highCount);
    report.setMediumIssues((int) mediumCount);
    report.setLowIssues((int) lowCount);

    // Add findings to report
    findings.forEach(report::addFinding);
  }

  private io.kubernetes.client.openapi.models.V1Ingress parseIngress(
      Map<String, Object> resource) {
    try {
      io.kubernetes.client.openapi.models.V1Ingress ingress =
          new io.kubernetes.client.openapi.models.V1Ingress();

      // Parse metadata
      @SuppressWarnings("unchecked")
      Map<String, Object> metadata = (Map<String, Object>) resource.get("metadata");
      if (metadata != null) {
        V1ObjectMeta objectMeta = new V1ObjectMeta();
        objectMeta.setName((String) metadata.get("name"));
        objectMeta.setNamespace((String) metadata.get("namespace"));
        ingress.setMetadata(objectMeta);
      }

      // Parse spec
      @SuppressWarnings("unchecked")
      Map<String, Object> spec = (Map<String, Object>) resource.get("spec");
      if (spec != null) {
        io.kubernetes.client.openapi.models.V1IngressSpec ingressSpec =
            new io.kubernetes.client.openapi.models.V1IngressSpec();

        // Check for TLS configuration
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> tls = (List<Map<String, Object>>) spec.get("tls");
        if (tls != null && !tls.isEmpty()) {
          List<io.kubernetes.client.openapi.models.V1IngressTLS> tlsList = new ArrayList<>();
          for (Map<String, Object> tlsEntry : tls) {
            io.kubernetes.client.openapi.models.V1IngressTLS ingressTls =
                new io.kubernetes.client.openapi.models.V1IngressTLS();
            @SuppressWarnings("unchecked")
            List<String> hosts = (List<String>) tlsEntry.get("hosts");
            ingressTls.setHosts(hosts);
            ingressTls.setSecretName((String) tlsEntry.get("secretName"));
            tlsList.add(ingressTls);
          }
          ingressSpec.setTls(tlsList);
        }

        ingress.setSpec(ingressSpec);
      }

      return ingress;
    } catch (Exception e) {
      logger.error("Error parsing Ingress: ", e);
      return null;
    }
  }

  private io.kubernetes.client.openapi.models.V1NetworkPolicy parseNetworkPolicy(
      Map<String, Object> resource) {
    try {
      io.kubernetes.client.openapi.models.V1NetworkPolicy networkPolicy =
          new io.kubernetes.client.openapi.models.V1NetworkPolicy();

      // Parse metadata
      @SuppressWarnings("unchecked")
      Map<String, Object> metadata = (Map<String, Object>) resource.get("metadata");
      if (metadata != null) {
        V1ObjectMeta objectMeta = new V1ObjectMeta();
        objectMeta.setName((String) metadata.get("name"));
        objectMeta.setNamespace((String) metadata.get("namespace"));
        networkPolicy.setMetadata(objectMeta);
      }

      return networkPolicy;
    } catch (Exception e) {
      logger.error("Error parsing NetworkPolicy: ", e);
      return null;
    }
  }

  private io.kubernetes.client.openapi.models.V1Role parseRole(Map<String, Object> resource) {
    try {
      io.kubernetes.client.openapi.models.V1Role role =
          new io.kubernetes.client.openapi.models.V1Role();

      // Parse metadata
      @SuppressWarnings("unchecked")
      Map<String, Object> metadata = (Map<String, Object>) resource.get("metadata");
      if (metadata != null) {
        V1ObjectMeta objectMeta = new V1ObjectMeta();
        objectMeta.setName((String) metadata.get("name"));
        objectMeta.setNamespace((String) metadata.get("namespace"));
        role.setMetadata(objectMeta);
      }

      // Parse rules
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> rules = (List<Map<String, Object>>) resource.get("rules");
      if (rules != null) {
        List<io.kubernetes.client.openapi.models.V1PolicyRule> policyRules = new ArrayList<>();
        for (Map<String, Object> ruleMap : rules) {
          io.kubernetes.client.openapi.models.V1PolicyRule policyRule =
              new io.kubernetes.client.openapi.models.V1PolicyRule();

          @SuppressWarnings("unchecked")
          List<String> verbs = (List<String>) ruleMap.get("verbs");
          policyRule.setVerbs(verbs);

          @SuppressWarnings("unchecked")
          List<String> resources = (List<String>) ruleMap.get("resources");
          policyRule.setResources(resources);

          @SuppressWarnings("unchecked")
          List<String> apiGroups = (List<String>) ruleMap.get("apiGroups");
          policyRule.setApiGroups(apiGroups);

          policyRules.add(policyRule);
        }
        role.setRules(policyRules);
      }

      return role;
    } catch (Exception e) {
      logger.error("Error parsing Role: ", e);
      return null;
    }
  }

  private io.kubernetes.client.openapi.models.V1ClusterRole parseClusterRole(
      Map<String, Object> resource) {
    try {
      io.kubernetes.client.openapi.models.V1ClusterRole clusterRole =
          new io.kubernetes.client.openapi.models.V1ClusterRole();

      // Parse metadata
      @SuppressWarnings("unchecked")
      Map<String, Object> metadata = (Map<String, Object>) resource.get("metadata");
      if (metadata != null) {
        V1ObjectMeta objectMeta = new V1ObjectMeta();
        objectMeta.setName((String) metadata.get("name"));
        clusterRole.setMetadata(objectMeta);
      }

      // Parse rules
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> rules = (List<Map<String, Object>>) resource.get("rules");
      if (rules != null) {
        List<io.kubernetes.client.openapi.models.V1PolicyRule> policyRules = new ArrayList<>();
        for (Map<String, Object> ruleMap : rules) {
          io.kubernetes.client.openapi.models.V1PolicyRule policyRule =
              new io.kubernetes.client.openapi.models.V1PolicyRule();

          @SuppressWarnings("unchecked")
          List<String> verbs = (List<String>) ruleMap.get("verbs");
          policyRule.setVerbs(verbs);

          @SuppressWarnings("unchecked")
          List<String> resources = (List<String>) ruleMap.get("resources");
          policyRule.setResources(resources);

          @SuppressWarnings("unchecked")
          List<String> apiGroups = (List<String>) ruleMap.get("apiGroups");
          policyRule.setApiGroups(apiGroups);

          policyRules.add(policyRule);
        }
        clusterRole.setRules(policyRules);
      }

      return clusterRole;
    } catch (Exception e) {
      logger.error("Error parsing ClusterRole: ", e);
      return null;
    }
  }

  public Optional<ScanReport> getScanReport(String scanId) {
    return scanReportRepository.findByScanId(scanId);
  }
}
