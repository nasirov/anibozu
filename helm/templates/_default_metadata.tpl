{{/* Generate basic metadata */}}
{{- define "default_metadata" }}
name: {{ template "chart.name" . }}
{{- include "extended_labels" . }}
{{- end }}
