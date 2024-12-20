package vttp.batch5.ssf.noticeboard.services;

import java.io.StringReader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import vttp.batch5.ssf.noticeboard.models.Notice;
import vttp.batch5.ssf.noticeboard.repositories.NoticeRepository;
import vttp.batch5.ssf.noticeboard.utilities.Helper;
import vttp.batch5.ssf.noticeboard.utilities.Redis;

@Service
public class NoticeService {
	private final RestTemplate restTemplate = new RestTemplate();

	@Value("${publishserver.url}")
    private String publishUrl;

	@Autowired
	private NoticeRepository noticeRepository;

	// TODO: Task 3
	// You can change the signature of this method by adding any number of parameters
	// and return any type
	public String postToNoticeServer(Notice notice) throws Exception {
		RequestEntity<String> request = buildRequest(notice);

		// Handling response
		return handleResponse(request);
	}


	private RequestEntity<String> buildRequest(Notice notice) {
		// Build Json payload for posting to REST API
		JsonArrayBuilder categoriesBuilder = Json.createArrayBuilder();
		for (String c : notice.getCategories()) {
			categoriesBuilder.add(c);
		}

		JsonObject jsonNotice = Json.createObjectBuilder()
			.add("title", notice.getTitle())
			.add("poster", notice.getPoster())
			.add("postDate", Helper.dateToLong(notice.getPostDate()))
			.add("categories", categoriesBuilder.build())
			.add("text", notice.getText())
			.build();
		
		// Building request to REST API
		String uri = UriComponentsBuilder.fromHttpUrl(publishUrl)
			.pathSegment("notice")
			.toUriString();

		RequestEntity<String> request = RequestEntity.post(uri)
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON)
			.body(jsonNotice.toString());

		return request;
	}
	

	private String handleResponse(RequestEntity<String> request) throws Exception {
		try {
			ResponseEntity<String> response = restTemplate.exchange(request, String.class);

			JsonReader reader = Json.createReader(new StringReader(response.getBody()));
			JsonObject payload = reader.readObject();

			// Write to Redis DB
			writeToDB(payload.getString("id"), payload.toString());
			return payload.getString("id");

		} catch (HttpStatusCodeException e) {
			throw errorHandling(e.getResponseBodyAsString());
		}
	}


	private Exception errorHandling(String body) {
		JsonReader reader = Json.createReader(new StringReader(body));

		try {
			JsonObject payload = reader.readObject();
			return new Exception(payload.getString("message"));
		} catch (JsonException e) {
			// If responsebody is not in json format.
			return new Exception(body);
		}
	}


	private void writeToDB(String hashKey, String data) {
		noticeRepository.insertNotices(Redis.KEY_NOTICES, hashKey, data);
	}


	public String checkRepo() {
		return noticeRepository.getRandomKey();
	}
}
