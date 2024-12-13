package vttp.batch5.ssf.noticeboard.services;

import java.io.StringReader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
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
			
		// Handling response
		ResponseEntity<String> response = restTemplate.exchange(request, String.class);
		return handleResponse(response);
	}

	private String handleResponse(ResponseEntity<String> response) throws Exception {
		JsonReader reader = Json.createReader(new StringReader(response.getBody()));
		JsonObject payload = reader.readObject();

		if (response.getStatusCode().is2xxSuccessful() || payload.containsKey("id")) {
			// Write to Redis DB
			writeToDB(payload.getString("id"), payload.toString());
			return payload.getString("id");
		} else {
			throw new Exception(payload.getString("message"));
		}

	}

	private void writeToDB(String hashKey, String data) {
		noticeRepository.insertNotices(Redis.KEY_NOTICES, hashKey, data);
	}

	public String checkRepo() {
		return noticeRepository.getRandomKey();
	}
}
