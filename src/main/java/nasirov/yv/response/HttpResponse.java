package nasirov.yv.response;

/**
 * Created by Хикка on 05.01.2019.
 */
public class HttpResponse {
	private String content;
	
	private Integer status;
	
	public HttpResponse() {
	}
	
	public HttpResponse(String content, Integer status) {
		this.content = content;
		this.status = status;
	}
	
	public String getContent() {
		return content;
	}
	
	public void setContent(String content) {
		this.content = content;
	}
	
	public Integer getStatus() {
		return status;
	}
	
	public void setStatus(Integer status) {
		this.status = status;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		HttpResponse that = (HttpResponse) o;
		if (!content.equals(that.content)) return false;
		return status.equals(that.status);
	}
	
	@Override
	public int hashCode() {
		int result = content.hashCode();
		result = 31 * result + status.hashCode();
		return result;
	}
	
	@Override
	public String toString() {
		return "HttpResponse{" +
				"content='" + content + '\'' +
				", status=" + status +
				'}';
	}
}
