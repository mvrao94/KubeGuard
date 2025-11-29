{{/*
Expand the name of the chart.
*/}}
{{- define "kubeguard.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
*/}}
{{- define "kubeguard.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "kubeguard.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "kubeguard.labels" -}}
helm.sh/chart: {{ include "kubeguard.chart" . }}
{{ include "kubeguard.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "kubeguard.selectorLabels" -}}
app.kubernetes.io/name: {{ include "kubeguard.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Create the name of the service account to use
*/}}
{{- define "kubeguard.serviceAccountName" -}}
{{- if .Values.serviceAccount.create }}
{{- default (include "kubeguard.fullname" .) .Values.serviceAccount.name }}
{{- else }}
{{- default "default" .Values.serviceAccount.name }}
{{- end }}
{{- end }}

{{/*
PostgreSQL fullname
*/}}
{{- define "kubeguard.postgres.fullname" -}}
{{- printf "%s-postgres" (include "kubeguard.fullname" .) }}
{{- end }}

{{/*
ConfigMap name
*/}}
{{- define "kubeguard.configmap.name" -}}
{{- if .Values.configMap.create }}
{{- default (printf "%s-config" (include "kubeguard.fullname" .)) .Values.configMap.name }}
{{- else }}
{{- .Values.configMap.name }}
{{- end }}
{{- end }}

{{/*
Secret name
*/}}
{{- define "kubeguard.secret.name" -}}
{{- default (printf "%s-secrets" (include "kubeguard.fullname" .)) .Values.secretName }}
{{- end }}
