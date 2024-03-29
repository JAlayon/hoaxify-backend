package com.alayon.hoaxify.hoax.controller;

import static com.alayon.hoaxify.utils.TestUtil.authenticate;
import static com.alayon.hoaxify.utils.TestUtil.getValidHoax;
import static com.alayon.hoaxify.utils.TestUtil.getValidUserForRequest;
import static org.assertj.core.api.Assertions.assertThat;

import com.alayon.hoaxify.hoax.dto.HoaxResponse;
import com.alayon.hoaxify.hoax.repository.HoaxRepository;
import com.alayon.hoaxify.hoax.service.HoaxService;
import com.alayon.hoaxify.user.model.User;
import com.alayon.hoaxify.utils.TestPage;
import com.alayon.hoaxify.utils.TestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.alayon.hoaxify.error.ApiError;
import com.alayon.hoaxify.hoax.model.Hoax;
import com.alayon.hoaxify.user.repository.UserRepository;
import com.alayon.hoaxify.user.service.UserService;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class HoaxControllerTest {

	private static final String API_HOAXES = "/api/v1/hoaxes";

	@Autowired
	private TestRestTemplate testRestTemplate;

	@Autowired
	UserService userService;

	@Autowired
	UserRepository userRepository;

	@Autowired
	HoaxRepository hoaxRepository;

	@Autowired
	HoaxService hoaxService;

	@PersistenceUnit
	EntityManagerFactory entityManagerFactory;

	@Before
	public void cleanup() {
		userRepository.deleteAll();
		hoaxRepository.deleteAll();
		testRestTemplate.getRestTemplate().getInterceptors().clear();
	}

	@After
	public void cleanupAfter(){
		hoaxRepository.deleteAll();
	}

	@Test
	public void postHoax_whenHoaxIsValidAndUserIsAuthorized_receiveOk() {
		userService.save(getValidUserForRequest("user1"));
		authenticate(testRestTemplate, "user1");

		final Hoax hoax = getValidHoax();
		final ResponseEntity<Object> response = postHoax(hoax, Object.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	public void postHoax_whenHoaxIsValidAndUserIsUnauthorized_receiveUnauthorized() {

		final Hoax hoax = getValidHoax();
		final ResponseEntity<Object> response = postHoax(hoax, Object.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Test
	public void postHoax_whenHoaxIsValidAndUserIsUnauthorized_receiveApiError() {

		final Hoax hoax = getValidHoax();
		final ResponseEntity<ApiError> response = postHoax(hoax, ApiError.class);

		assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
	}

	@Test
	public void postHoax_whenHoaxIsValidAndUserIsAuthorized_hoaxSavedToDB() {
		userService.save(getValidUserForRequest("user1"));
		authenticate(testRestTemplate, "user1");

		final Hoax hoax = getValidHoax();
		postHoax(hoax, Object.class);

		assertThat(hoaxRepository.count()).isEqualTo(1);
	}

	@Test
	public void postHoax_whenHoaxIsValidAndUserIsAuthorized_hoaxSavedToDBWithTimestamp() {
		userService.save(getValidUserForRequest("user1"));
		authenticate(testRestTemplate, "user1");

		final Hoax hoax = getValidHoax();
		postHoax(hoax, Object.class);

		Hoax hoaxInDb = hoaxRepository.findAll().get(0);

		assertThat(hoaxInDb.getTimestamp()).isNotNull();
	}

	@Test
	public void postHoax_whenHoaxContentIsNullAndUserIsAuthorized_receiveBadRequest() {
		userService.save(getValidUserForRequest("user1"));
		authenticate(testRestTemplate, "user1");

		final Hoax hoax = new Hoax();
		final ResponseEntity<Object> response = postHoax(hoax, Object.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void postHoax_whenHoaxContentLessThan10CharactersAndUserIsAuthorized_receiveBadRequest() {
		userService.save(getValidUserForRequest("user1"));
		authenticate(testRestTemplate, "user1");

		final Hoax hoax = new Hoax();
		hoax.setContent("123456789");
		final ResponseEntity<Object> response = postHoax(hoax, Object.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void postHoax_whenHoaxContentHas5000CharactersAndUserIsAuthorized_receiveBadRequest() {
		userService.save(getValidUserForRequest("user1"));
		authenticate(testRestTemplate, "user1");

		final Hoax hoax = new Hoax();
		String longString = IntStream.rangeClosed(1,5000).mapToObj(i -> "x").collect(Collectors.joining());
		hoax.setContent(longString);
		final ResponseEntity<Object> response = postHoax(hoax, Object.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	public void postHoax_whenHoaxContentHasMoreThan5000CharactersAndUserIsAuthorized_receiveBadRequest() {
		userService.save(getValidUserForRequest("user1"));
		authenticate(testRestTemplate, "user1");

		final Hoax hoax = new Hoax();
		String longString = IntStream.rangeClosed(1,5001).mapToObj(i -> "x").collect(Collectors.joining());
		hoax.setContent(longString);
		final ResponseEntity<Object> response = postHoax(hoax, Object.class);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void postHoax_whenHoaxContentIsNullAndUserIsAuthorized_receiveApiErrorWithValidationErrors() {
		userService.save(getValidUserForRequest("user1"));
		authenticate(testRestTemplate, "user1");

		final Hoax hoax = new Hoax();
		final ResponseEntity<ApiError> response = postHoax(hoax, ApiError.class);
		final Map<String, String> validationErrors = response.getBody().getValidationErrors();

		assertThat(validationErrors.get("content")).isNotNull();
	}

	@Test
	public void postHoax_whenHoaxIsValidAndUserIsAuthorized_hoaxSavedWithAuthenticatedUserInfo() {
		userService.save(getValidUserForRequest("user1"));
		authenticate(testRestTemplate, "user1");

		final Hoax hoax = getValidHoax();
		postHoax(hoax, Object.class);

		Hoax hoaxInDb = hoaxRepository.findAll().get(0);

		assertThat(hoaxInDb.getUser().getUsername()).isEqualTo("user1");
	}

	@Test
	public void postHoax_whenHoaxIsValidAndUserIsAuthorized_hoaxCanBeAccessedFromUserEntity() {
		User user = userService.save(getValidUserForRequest("user1"));
		authenticate(testRestTemplate, "user1");

		final Hoax hoax = getValidHoax();
		postHoax(hoax, Object.class);

		EntityManager entityManager = entityManagerFactory.createEntityManager();
		User userInDb = entityManager.find(User.class, user.getId());
		assertThat(userInDb.getHoaxes().size()).isEqualTo(1);
	}

	@Test
	public void postHoax_whenHoaxIsValidAndUserIsAuthorized_receiveHoaxResponse() {
		User user = userService.save(getValidUserForRequest("user1"));
		authenticate(testRestTemplate, "user1");

		final Hoax hoax = getValidHoax();
		ResponseEntity<HoaxResponse> response = postHoax(hoax, HoaxResponse.class);
		assertThat(response.getBody().getUser().getUsername()).isEqualTo("user1");
	}

	@Test
	public void getHoaxes_whenThereAreNoHoaxes_receiveOk(){
		ResponseEntity<?> response = testRestTemplate.getForEntity(API_HOAXES, null);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	public void getHoaxes_whenThereAreNoHoaxes_receivePageWithZeroElements(){
		ResponseEntity<TestPage<Object>> response = getHoaxes(new ParameterizedTypeReference<TestPage<Object>>() {
		});
		assertThat(response.getBody().getTotalElements()).isEqualTo(0);
	}

	@Test
	public void getHoaxes_whenThereAreHoaxes_receivePageWithElements(){
		User user = userService.save(TestUtil.getValidUserForRequest("user1"));
		hoaxService.saveHoax(TestUtil.getValidHoaxRequest(), user);
		hoaxService.saveHoax(TestUtil.getValidHoaxRequest(), user);
		hoaxService.saveHoax(TestUtil.getValidHoaxRequest(), user);

		ResponseEntity<TestPage<Object>> response = getHoaxes(new ParameterizedTypeReference<TestPage<Object>>() {
		});
		assertThat(response.getBody().getTotalElements()).isEqualTo(3);
	}

	@Test
	public void getHoaxes_whenThereAreHoaxes_receivePageWithHoaxResponse(){
		User user = userService.save(TestUtil.getValidUserForRequest("user1"));
		hoaxService.saveHoax(TestUtil.getValidHoaxRequest(), user);

		ResponseEntity<TestPage<HoaxResponse>> response = getHoaxes(new ParameterizedTypeReference<TestPage<HoaxResponse>>() {
		});
		HoaxResponse hoaxResponse = response.getBody().getContent().get(0);
		assertThat(hoaxResponse.getUser().getUsername()).isEqualTo("user1");
	}

	@Test
	public void getHoaxesByUsername_whenUserExists_receiveOk(){
		userService.save(TestUtil.getValidUserForRequest("user1"));
		ResponseEntity<Object> response = getHoaxesByUsername("user1", new ParameterizedTypeReference<Object>() {
		});
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	public void getHoaxesByUsername_whenUserDoesNotExists_receiveNotFound(){
		ResponseEntity<Object> response =  getHoaxesByUsername("unknown-user", new ParameterizedTypeReference<Object>() {
		});
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	public void getHoaxesByUsername_whenUserExists_receivePageWithZeroHoaxes(){
		userService.save(TestUtil.getValidUserForRequest("user1"));
		ResponseEntity<TestPage<Object>> response = getHoaxesByUsername("user1", new ParameterizedTypeReference<TestPage<Object>>() {
		});
		assertThat(response.getBody().getTotalElements()).isEqualTo(0);
	}

	@Test
	public void getHoaxesByUsername_whenUserExistsWithHoax_receivePageWithHoaxResponse(){
		User user = userService.save(TestUtil.getValidUserForRequest("user1"));
		hoaxService.saveHoax(TestUtil.getValidHoaxRequest(), user);

		ResponseEntity<TestPage<HoaxResponse>> response = getHoaxesByUsername("user1", new ParameterizedTypeReference<TestPage<HoaxResponse>>() {
		});
		HoaxResponse hoaxStored = response.getBody().getContent().get(0);
		assertThat(hoaxStored.getUser().getUsername()).isEqualTo("user1");
	}

	@Test
	public void getHoaxesByUsername_whenUserExistsWithMultipleHoaxes_receivePageWithMatchingHoaxesCount(){
		User user = userService.save(TestUtil.getValidUserForRequest("user1"));
		hoaxService.saveHoax(TestUtil.getValidHoaxRequest(), user);
		hoaxService.saveHoax(TestUtil.getValidHoaxRequest(), user);
		hoaxService.saveHoax(TestUtil.getValidHoaxRequest(), user);

		ResponseEntity<TestPage<HoaxResponse>> response = getHoaxesByUsername("user1", new ParameterizedTypeReference<TestPage<HoaxResponse>>() {
		});
		assertThat(response.getBody().getTotalElements()).isEqualTo(3);
	}

	@Test
	public void getHoaxesByUsername_whenMultipleUsersExistWithMultipleHoaxes_receivePageWithMatchingHoaxesCount(){
		User userWithThreeHoaxes = userService.save(TestUtil.getValidUserForRequest("user1"));
		IntStream.rangeClosed(1,3).forEach(i -> {
			hoaxService.saveHoax(TestUtil.getValidHoaxRequest(), userWithThreeHoaxes);
		});

		User userWithFiveHoaxes = userService.save(TestUtil.getValidUserForRequest("user2"));
		IntStream.rangeClosed(1,5).forEach(i -> {
			hoaxService.saveHoax(TestUtil.getValidHoaxRequest(), userWithFiveHoaxes);
		});

		ResponseEntity<TestPage<HoaxResponse>> response = getHoaxesByUsername(userWithFiveHoaxes.getUsername(), new ParameterizedTypeReference<TestPage<HoaxResponse>>() {
		});
		assertThat(response.getBody().getTotalElements()).isEqualTo(5);
	}

	@Test
	public void getOldHoaxes_whenThereAreNoHoaxes_receiveOk(){
		ResponseEntity<?> response = getOldHoaxes(5, new ParameterizedTypeReference<Object>() {
		});
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	public void getOldHoaxes_whenThereAreHoaxes_receivePageWithItemsProvidedId(){
		User user = userService.save(getValidUserForRequest("user1"));
		IntStream.rangeClosed(1,4).forEach(i -> hoaxService.saveHoax(TestUtil.getValidHoaxRequest(), user));
		Hoax fifthHoax = hoaxService.saveHoax(TestUtil.getValidHoaxRequest(), user);
		ResponseEntity<TestPage<Object>> response = getOldHoaxes(fifthHoax.getId(), new ParameterizedTypeReference<TestPage<Object>>() {
		});
		assertThat(response.getBody().getTotalElements()).isEqualTo(4);
	}

	@Test
	public void getOldHoaxesOfUser_whenUserExistThereAreNoHoaxes_receiveOk(){
		userService.save(getValidUserForRequest("user1"));
		ResponseEntity<?> response = getOldHoaxesByUser(5, "user1", new ParameterizedTypeReference<Object>() {
		});
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	public void getOldHoaxesOfUser_whenUserExistAndThereAreHoaxes_receivePageWithItemsProvidedId(){
		User user = userService.save(getValidUserForRequest("user1"));
		IntStream.rangeClosed(1,4).forEach(i -> hoaxService.saveHoax(TestUtil.getValidHoaxRequest(), user));
		Hoax fifthHoax = hoaxService.saveHoax(TestUtil.getValidHoaxRequest(), user);
		ResponseEntity<TestPage<Object>> response = getOldHoaxesByUser(fifthHoax.getId(), "user1",  new ParameterizedTypeReference<TestPage<Object>>() {
		});
		assertThat(response.getBody().getTotalElements()).isEqualTo(4);
	}

	@Test
	public void getOldHoaxesOfUser_whenUserDoesNotExistThereAreNoHoaxes_receiveNotFound(){
		ResponseEntity<?> response = getOldHoaxesByUser(1, "user1",  new ParameterizedTypeReference<Object>() {
		});
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	public void getOldHoaxesOfUser_whenUserExistAndThereAreNoHoaxes_receivePagWithZeroItemBeforeProvidedId(){
		User user = userService.save(getValidUserForRequest("user1"));
		IntStream.rangeClosed(1,4).forEach(i -> hoaxService.saveHoax(TestUtil.getValidHoaxRequest(), user));
		Hoax fifthHoax = hoaxService.saveHoax(TestUtil.getValidHoaxRequest(), user);

		userService.save(getValidUserForRequest("user2"));
		ResponseEntity<TestPage<Object>> response = getOldHoaxesByUser(fifthHoax.getId(), "user2",  new ParameterizedTypeReference<TestPage<Object>>() {
		});
		assertThat(response.getBody().getTotalElements()).isEqualTo(0);
	}

	@Test
	public void getNewHoaxes_whenThereAreHoaxes_receiveListOfItemsAfterProvidedId(){
		User user = userService.save(TestUtil.getValidUserForRequest("user1"));
		IntStream.rangeClosed(1,4).forEach(i -> hoaxService.saveHoax(TestUtil.getValidHoaxRequest(), user));
		Hoax fifthHoax = hoaxService.saveHoax(TestUtil.getValidHoaxRequest(), user);
		hoaxService.saveHoax(TestUtil.getValidHoaxRequest(), user);
		ResponseEntity<List<Object>> response = getNewHoaxes(fifthHoax.getId(), new ParameterizedTypeReference<List<Object>>() {
		});
		assertThat(response.getBody().size()).isEqualTo(1);
	}

	@Test
	public void getNewHoaxes_whenThereAreHoaxes_receiveListOfHoaxResponseAfterProvidedId(){
		User user = userService.save(TestUtil.getValidUserForRequest("user1"));
		IntStream.rangeClosed(1,4).forEach(i -> hoaxService.saveHoax(TestUtil.getValidHoaxRequest(), user));
		Hoax fifthHoax = hoaxService.saveHoax(TestUtil.getValidHoaxRequest(), user);
		hoaxService.saveHoax(TestUtil.getValidHoaxRequest(), user);
		ResponseEntity<List<HoaxResponse>> response = getNewHoaxes(fifthHoax.getId(), new ParameterizedTypeReference<List<HoaxResponse>>() {
		});
		assertThat(response.getBody().get(0).getDate()).isGreaterThan(0);
	}

	@Test
	public void getNewHoaxCount_whenThereAreHoaxes_receiveCountAfterProvidedId(){
		User user = userService.save(TestUtil.getValidUserForRequest("user1"));
		IntStream.rangeClosed(1,4).forEach(i -> hoaxService.saveHoax(TestUtil.getValidHoaxRequest(), user));
		Hoax fifthHoax = hoaxService.saveHoax(TestUtil.getValidHoaxRequest(), user);
		hoaxService.saveHoax(TestUtil.getValidHoaxRequest(), user);
		ResponseEntity<Map<String,Long>> response = getNewHoaxCount(fifthHoax.getId(), new ParameterizedTypeReference<Map<String,Long>>() {
		});
		assertThat(response.getBody().get("count")).isEqualTo(1);
	}

	@Test
	public void getNewHoaxCountOfUser_whenThereAreHoaxes_receiveCountAfterProvidedId(){
		User user = userService.save(TestUtil.getValidUserForRequest("user1"));
		IntStream.rangeClosed(1,4).forEach(i -> hoaxService.saveHoax(TestUtil.getValidHoaxRequest(), user));
		Hoax fifthHoax = hoaxService.saveHoax(TestUtil.getValidHoaxRequest(), user);
		hoaxService.saveHoax(TestUtil.getValidHoaxRequest(), user);
		ResponseEntity<Map<String,Long>> response = getNewHoaxCountOfUser(fifthHoax.getId(),"user1", new ParameterizedTypeReference<Map<String,Long>>() {
		});
		assertThat(response.getBody().get("count")).isEqualTo(1);
	}

	private <T> ResponseEntity<T> getNewHoaxCount(long hoaxId, ParameterizedTypeReference<T> responseType){
		String path = API_HOAXES + "/" + hoaxId + "?direction=after&count=true";
		return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
	}

	private <T> ResponseEntity<T> getNewHoaxCountOfUser(long hoaxId, String username, ParameterizedTypeReference<T> responseType){
		String path =  "/api/v1/users/" + username + "/hoaxes/" + hoaxId + "?direction=after&count=true";
		return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
	}

	private <T> ResponseEntity<T> getNewHoaxes(long hoaxId, ParameterizedTypeReference<T> responseType){
		String path = API_HOAXES + "/" + hoaxId + "?direction=after&sort=id,desc";
		return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
	}

	private <T> ResponseEntity<T> getOldHoaxes(long hoaxId, ParameterizedTypeReference<T> responseType){
		String path = API_HOAXES + "/" + hoaxId + "?direction=before&page=0&size=5&sort=id,desc";
		return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
	}

	private <T> ResponseEntity<T> getOldHoaxesByUser(long hoaxId, String username, ParameterizedTypeReference<T> responseType){
		String path = "/api/v1/users/" + username + "/hoaxes/" + hoaxId + "?direction=before&page=0&size=5&sort=id,desc";
		return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
	}

	private <T> ResponseEntity<T> getHoaxesByUsername(String username, ParameterizedTypeReference<T> responseType){
		String path = "/api/v1/users/" + username + "/hoaxes";
		return testRestTemplate.exchange(path,HttpMethod.GET, null, responseType);
	}

	private <T> ResponseEntity<T> getHoaxes(final ParameterizedTypeReference<T> responseType){
		return testRestTemplate.exchange(API_HOAXES, HttpMethod.GET, null, responseType);
	}

	private <T> ResponseEntity<T> postHoax(final Hoax hoax, final Class<T> responseType) {
		return testRestTemplate.postForEntity(API_HOAXES, hoax, responseType);
	}

}
