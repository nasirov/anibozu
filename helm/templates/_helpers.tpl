{{- define "anibozu.metadata" }}
name: {{ .Chart.Name }}
namespace: {{ .Release.Namespace }}
{{- end }}

{{- define "anibozu.labels" }}
app.kubernetes.io/name: {{ .Chart.Name }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/version: {{ .Chart.AppVersion }}
app.kubernetes.io/part-of: {{ .Chart.Description }}
app.kubernetes.io/component: backend
helm.sh/chart: {{ .Chart.Name }}-{{ .Chart.Version }}
{{- end }}

{{- define "anibozu.annotations" }}
meta.helm.sh/release-name: {{ .Chart.Name }}
meta.helm.sh/release-namespace: {{ .Release.Namespace }}
{{- end }}

{{- define "vault.injector.service-account" }}
{{- if .Values.vault.enabled }}
serviceAccountName: {{ .Chart.Name }}
{{- end }}
{{- end }}

{{- define "vault.injector.annotations" }}
{{- if .Values.vault.enabled }}
vault.hashicorp.com/agent-inject: "true"
vault.hashicorp.com/role: {{ .Chart.Name }}
vault.hashicorp.com/agent-pre-populate-only: "true"
vault.hashicorp.com/agent-init-first: "true"
{{- range .Values.vault.secrets }}
vault.hashicorp.com/agent-inject-secret-{{ .name }}: {{ .path | quote }}
vault.hashicorp.com/agent-inject-template-{{ .name }}: |
  {{printf "{{- with secret "}}{{ .path | quote }}{{` -}}`}}
  {{- range .variables}}
  export {{ .bindTo }}{{ printf "=\"{{ .Data.data."}}{{ .name }} {{`}}"`}}
  {{- end }}
  {{`{{- end }}`}}
{{- end }}
{{- end }}
{{- end }}