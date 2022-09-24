{{/* Generate basic labels */}}
{{- define "extended_labels" }}
labels:
  app: {{ template "chart.name" . }}
  chart: {{ template "chart.name" . }}-{{ .Chart.Version }}
  version: {{ .Chart.Version }}
{{- end }}

{{- define "default_labels" }}
app: {{ template "chart.name" . }}
{{- end }}
