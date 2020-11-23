package com.alayon.hoaxify.controllers;

import static com.alayon.hoaxify.controllers.TestUtil.getValidUser;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.alayon.hoaxify.commons.GenericResponse;
import com.alayon.hoaxify.config.AppConfiguration;
import com.alayon.hoaxify.error.ApiError;
import com.alayon.hoaxify.user.TestPage;
import com.alayon.hoaxify.user.User;
import com.alayon.hoaxify.user.UserRepository;
import com.alayon.hoaxify.user.UserService;
import com.alayon.hoaxify.user.dto.UserDto;
import com.alayon.hoaxify.user.dto.UserUpdateDto;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class UserControllerTest {

	private static final String API_USERS = "/api/v1/users";

	@Autowired
	TestRestTemplate testRestTemplate;

	@Autowired
	UserRepository userRepository;

	@Autowired
	UserService userService;

	@Autowired
	AppConfiguration appConfig;

	@Before
	public void cleanup() {
		userRepository.deleteAll();
		testRestTemplate.getRestTemplate().getInterceptors().clear();
	}

	@After
	public void cleanDirectory() throws IOException {
		FileUtils.cleanDirectory(new File(appConfig.getFullProfileImagePath()));
		FileUtils.cleanDirectory(new File(appConfig.getFullAttachmentPath()));
	}

	@Test
	public void postUser_whenUserIsValid_receiveOk() {
		final User user = getValidUser();
		final ResponseEntity<?> response = postSignup(user, null);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	public void postUser_whenUserIsValid_userSavedToDatabase() {
		final User user = getValidUser();
		testRestTemplate.postForEntity(API_USERS, user, null);
		assertThat(userRepository.count()).isEqualTo(1);

	}

	@Test
	public void postUser_whenUserIsValid_receiveSuccessMessage() {
		final User user = getValidUser();
		final ResponseEntity<GenericResponse> response = postSignup(user, GenericResponse.class);
		assertThat(response.getBody().getMessage()).isNotNull();
	}

	@Test
	public void postUser_whenUserIsValid_passwordIsHasheedInDb() {
		final User user = getValidUser();
		testRestTemplate.postForEntity(API_USERS, user, null);
		final List<User> users = userRepository.findAll();
		final User userInDb = users.get(0);
		assertThat(userInDb.getPassword()).isNotEqualTo(user.getPassword());
	}

	@Test
	public void postUser_whenUserHasNullUsername_receiveBadRequest() {
		final User user = getValidUser();
		user.setUsername(null);
		final ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void postUser_whenUserHasNullDisplayname_receiveBadRequest() {
		final User user = getValidUser();
		user.setDisplayname(null);
		final ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void postUser_whenUserHasNullPassword_receiveBadRequest() {
		final User user = getValidUser();
		user.setPassword(null);
		final ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void postUser_whenUserHasUsernameWithLessThanRequired_receiveBadRequest() {
		final User user = getValidUser();
		user.setUsername("abc");
		final ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void postUser_whenUserHasDisplaynameWithLessThanRequired_receiveBadRequest() {
		final User user = getValidUser();
		user.setDisplayname("abc");
		final ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void postUser_whenUserHasPasswordWithLessThanRequired_receiveBadRequest() {
		final User user = getValidUser();
		user.setPassword("P4ssw");
		final ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void postUser_whenUserHasUsernameExceedsTheLengthLimit_receiveBadRequest() {
		final User user = getValidUser();
		final String valueOf256Chars = IntStream.rangeClosed(1, 256).mapToObj(x -> "a").collect(Collectors.joining());
		user.setUsername(valueOf256Chars);
		final ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void postUser_whenUserHasDisplaynameExceedsTheLengthLimit_receiveBadRequest() {
		final User user = getValidUser();
		final String valueOf256Chars = IntStream.rangeClosed(1, 256).mapToObj(x -> "a").collect(Collectors.joining());
		user.setDisplayname(valueOf256Chars);
		final ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void postUser_whenUserHasPasswordExceedsTheLengthLimit_receiveBadRequest() {
		final User user = getValidUser();
		final String valueOf256Chars = IntStream.rangeClosed(1, 256).mapToObj(x -> "a").collect(Collectors.joining());
		user.setPassword(valueOf256Chars + "A1");
		final ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void postUser_whenUserHasPasswordWithAllLowercase_receiveBadRequest() {
		final User user = getValidUser();
		user.setPassword("alllowercase");
		final ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void postUser_whenUserHasPasswordWithAllUppercase_receiveBadRequest() {
		final User user = getValidUser();
		user.setPassword("ALLUPPERCASE");
		final ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void postUser_whenUserHasPasswordWithAllNumbers_receiveBadRequest() {
		final User user = getValidUser();
		user.setPassword("123456789");
		final ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void postUser_whenUserHasntBody_receiveApiError() {
		final User user = new User();
		final ResponseEntity<ApiError> response = postSignup(user, ApiError.class);
		assertThat(response.getBody().getUrl()).isEqualTo(API_USERS);
	}

	@Test
	public void postUser_whenUserHasNullUsername_receiveMessageOfNullErrorForUsername() {
		final User user = getValidUser();
		user.setUsername(null);
		final ResponseEntity<ApiError> response = postSignup(user, ApiError.class);
		final Map<String, String> validationErrors = response.getBody().getValidationErrors();
		assertThat(validationErrors.get("username")).isEqualTo("Username cannot be null");
	}

	@Test
	public void postUser_whenUserHasNullPassword_receiveMessageOfNullError() {
		final User user = getValidUser();
		user.setPassword(null);
		final ResponseEntity<ApiError> response = postSignup(user, ApiError.class);
		final Map<String, String> validationErrors = response.getBody().getValidationErrors();
		assertThat(validationErrors.get("password")).isEqualTo("Cannot be null");
	}

	@Test
	public void postUser_whenUserHasInvalidLengthUsername_receiveGenericMessageOfSizeError() {
		final User user = getValidUser();
		user.setUsername("abc");
		final ResponseEntity<ApiError> response = postSignup(user, ApiError.class);
		final Map<String, String> validationErrors = response.getBody().getValidationErrors();
		assertThat(validationErrors.get("username")).isEqualTo("It must have minimum 4 and maximum 255 characters");
	}

	@Test
	public void postUser_whenUserHasInvalidPasswordPattern_receiveMessageOfPasswordPatternError() {
		final User user = getValidUser();
		user.setPassword("alllowercase");
		final ResponseEntity<ApiError> response = postSignup(user, ApiError.class);
		final Map<String, String> validationErrors = response.getBody().getValidationErrors();
		assertThat(validationErrors.get("password"))
				.isEqualTo("Password must have at least one uppercase, one lowercase letter and one number");
	}

	@Test
	public void postUser_whenAnotherUserHasSameUsername_receiveBadRequest() {
		userRepository.save(getValidUser());
		final User user = getValidUser();
		final ResponseEntity<?> response = postSignup(user, null);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void postUser_whenAnotherUserHasSameUsername_receiveMessageOfDuplicateUsername() {
		userRepository.save(getValidUser());
		final User user = getValidUser();
		final ResponseEntity<ApiError> response = postSignup(user, ApiError.class);
		final Map<String, String> validationErrors = response.getBody().getValidationErrors();
		assertThat(validationErrors.get("username")).isEqualTo("This name is in used");
	}

	@Test
	public void getUsers_whenThereAreNoUsersInDb_receiveOk() {
		final ResponseEntity<Object> response = testRestTemplate.getForEntity(API_USERS, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	public void getUsers_whenThereAreNoUsersInDb_receivePageWithZeroItems() {
		final ResponseEntity<TestPage<?>> response = testRestTemplate.exchange(API_USERS, HttpMethod.GET, null,
				new ParameterizedTypeReference<TestPage<?>>() {
				});
		assertThat(response.getBody().getTotalElements()).isEqualTo(0);
	}

	@Test
	public void getUsers_whenThereAreNoUsersInDb_receivePageWithUser() {
		userRepository.save(getValidUser());
		final ResponseEntity<TestPage<?>> response = getUsers(new ParameterizedTypeReference<TestPage<?>>() {
		});
		assertThat(response.getBody().getNumberOfElements()).isEqualTo(1);
	}

	@Test
	public void getUsers_whenThereIsAnUserInDb_receiveUserWithoutPassword() {
		userRepository.save(getValidUser());
		final ResponseEntity<TestPage<Map<String, Object>>> response = getUsers(
				new ParameterizedTypeReference<TestPage<Map<String, Object>>>() {
				});
		final Map<String, Object> entity = response.getBody().getContent().get(0);
		assertThat(entity.containsKey("password")).isFalse();
	}

	@Test
	public void getUsers_whenPageIsRequestedFor3ItemsPerPageWhereTheDbHas20Users_receive3Users() {
		IntStream.rangeClosed(1, 20).mapToObj(i -> "test-user-" + i).map(username -> TestUtil.getValidUser(username))
				.forEach(userRepository::save);
		final String path = API_USERS + "?page=0&size=3";
		final ResponseEntity<TestPage<?>> response = getUsers(path, new ParameterizedTypeReference<TestPage<?>>() {
		});
		assertThat(response.getBody().getContent().size()).isEqualTo(3);
	}

	@Test
	public void getUsers_whenPageSizeNotProvided_receivePageSizeAs10() {
		final ResponseEntity<TestPage<?>> response = getUsers(new ParameterizedTypeReference<TestPage<?>>() {
		});
		assertThat(response.getBody().getSize()).isEqualTo(10);
	}

	@Test
	public void getUsers_whenPageSizeIsGreaterThan100_receivePageSizeAs100() {
		final String path = API_USERS + "?size=500";
		final ResponseEntity<TestPage<?>> response = getUsers(path, new ParameterizedTypeReference<TestPage<?>>() {
		});
		assertThat(response.getBody().getSize()).isEqualTo(100);
	}

	@Test
	public void getUsers_whenPageSizeIsNegative_receivePageSizeAs10() {
		final String path = API_USERS + "?size=-5";
		final ResponseEntity<TestPage<?>> response = getUsers(path, new ParameterizedTypeReference<TestPage<?>>() {
		});
		assertThat(response.getBody().getSize()).isEqualTo(10);
	}

	@Test
	public void getUsers_whenPageSizeIsNegative_receiveFirstPage() {
		final String path = API_USERS + "?size=-5";
		final ResponseEntity<TestPage<?>> response = getUsers(path, new ParameterizedTypeReference<TestPage<?>>() {
		});
		assertThat(response.getBody().getNumber()).isEqualTo(0);
	}

	@Test
	public void getUsers_whenUserIsLoggedIn_receivePageWithoutLoggedInUser() {
		userService.save(getValidUser("user1"));
		userService.save(getValidUser("user2"));
		userService.save(getValidUser("user3"));
		authenticate("user1");
		final ResponseEntity<TestPage<?>> response = getUsers(new ParameterizedTypeReference<TestPage<?>>() {
		});
		assertThat(response.getBody().getTotalElements()).isEqualTo(2);
	}

	@Test
	public void getUserByUsername_whenUserExists_receiveOk() {
		final String username = "test-user";
		userService.save(getValidUser(username));
		final ResponseEntity<?> response = getUser(username, null);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	public void getUserByUsername_whenUserExists_receivUserWithoutPassword() {
		final String username = "test-user";
		userService.save(getValidUser(username));
		final ResponseEntity<String> response = getUser(username, String.class);
		assertThat(response.getBody().contains("password")).isFalse();
	}

	@Test
	public void getUserByUsername_whenUserDoesNotExist_receiveNotFound() {
		final ResponseEntity<?> response = getUser("test-user", null);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	public void getUserByUsername_whenUserDoesNotExist_receiveApiError() {
		final ResponseEntity<ApiError> response = getUser("unknown-user", ApiError.class);
		assertThat(response.getBody().getMessage().contains("unknown-user")).isTrue();
	}

	@Test
	public void putUser_whenUnauthorizedUserSendsTheRequest_receiveUnauthorized() {
		final ResponseEntity<Object> response = putUser(123, null, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Test
	public void putUser_whenAuthorizedUserSendsUpdateForAnotherUser_receiveForbidden() {
		final User user = userService.save(TestUtil.getValidUser("user1"));
		authenticate(user.getUsername());
		final long anotherUserId = user.getId() + 123;
		final ResponseEntity<Object> response = putUser(anotherUserId, null, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
	}

	@Test
	public void putUser_whenUnauthorizedUserSendsTheRequest_receiveApiError() {
		final ResponseEntity<ApiError> response = putUser(123, null, ApiError.class);
		assertThat(response.getBody().getUrl()).contains("users/123");
	}

	@Test
	public void putUser_whenAuthorizedUserSendsUpdateForAnotherUser_receiveApiError() {
		final User user = userService.save(TestUtil.getValidUser("user1"));
		authenticate(user.getUsername());
		final long anotherUserId = user.getId() + 123;
		final ResponseEntity<ApiError> response = putUser(anotherUserId, null, ApiError.class);
		assertThat(response.getBody().getUrl()).contains("users/" + anotherUserId);
	}

	@Test
	public void putUser_whenValidRequestBodyFromAuthorizedUser_receiveOk() {
		final User user = userService.save(TestUtil.getValidUser("user1"));
		authenticate(user.getUsername());

		final HttpEntity<UserUpdateDto> requestEntity = new HttpEntity<>(TestUtil.getValidUserUpdate());
		final ResponseEntity<?> response = putUser(user.getId(), requestEntity, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	public void putUser_whenValidRequestBodyFromAuthorizedUser_displayNameUpdated() {
		final User user = userService.save(TestUtil.getValidUser("user1"));
		authenticate(user.getUsername());

		final HttpEntity<UserUpdateDto> requestEntity = new HttpEntity<>(TestUtil.getValidUserUpdate());
		putUser(user.getId(), requestEntity, Object.class);

		final User userInDb = userRepository.findByUsername("user1");
		assertThat(userInDb.getDisplayname()).isEqualTo(TestUtil.getValidUserUpdate().getDisplayName());
	}

	@Test
	public void putUser_whenValidRequestBodyFromAuthorizedUser_receiveUserDtoWithUpdatedDisplayName() {
		final User user = userService.save(TestUtil.getValidUser("user1"));
		authenticate(user.getUsername());

		final HttpEntity<UserUpdateDto> requestEntity = new HttpEntity<>(TestUtil.getValidUserUpdate());
		final ResponseEntity<UserUpdateDto> response = putUser(user.getId(), requestEntity, UserUpdateDto.class);

		assertThat(response.getBody().getDisplayName()).isEqualTo(TestUtil.getValidUserUpdate().getDisplayName());
	}

	@Test
	public void putUser_whenValidRequestBodyWithSupportedImageFromAuthorizedUser_receiveUserDtoWithRandomImageName()
			throws IOException {
		final User user = userService.save(TestUtil.getValidUser("user1"));
		authenticate(user.getUsername());

		final UserUpdateDto userUpdateDto = TestUtil.getValidUserUpdate();
		final String imageString = readFileToBase64("user-profile.png");

		userUpdateDto.setImage(imageString);

		final HttpEntity<UserUpdateDto> requestEntity = new HttpEntity<>(userUpdateDto);
		final ResponseEntity<UserDto> response = putUser(user.getId(), requestEntity, UserDto.class);

		assertThat(response.getBody().getImage()).isNotEqualTo("profile-image.png");

	}

	@Test
	public void putUser_whenValidRequestBodyWithSupportedImageFromAuthorizedUser_imageIsStoredUnderProfileFolder()
			throws IOException {
		final User user = userService.save(TestUtil.getValidUser("user1"));
		authenticate(user.getUsername());

		final UserUpdateDto userUpdateDto = TestUtil.getValidUserUpdate();
		final String imageString = readFileToBase64("user-profile.png");

		userUpdateDto.setImage(imageString);

		final HttpEntity<UserUpdateDto> requestEntity = new HttpEntity<>(userUpdateDto);
		final ResponseEntity<UserDto> response = putUser(user.getId(), requestEntity, UserDto.class);

		final String storedImageName = response.getBody().getImage();

		final String profilePicturePath = appConfig.getFullProfileImagePath() + "/" + storedImageName;

		final File storedImage = new File(profilePicturePath);
		assertThat(storedImage.exists()).isTrue();

	}

	private String readFileToBase64(final String file) throws IOException {
		final ClassPathResource imageResource = new ClassPathResource(file);
		final byte[] imageArr = FileUtils.readFileToByteArray(imageResource.getFile());
		final String imageString = Base64.getEncoder().encodeToString(imageArr);
		return imageString;
	}

	private <T> ResponseEntity<T> postSignup(final Object request, final Class<T> response) {
		return testRestTemplate.postForEntity(API_USERS, request, response);
	}

	private <T> ResponseEntity<T> getUsers(final ParameterizedTypeReference<T> responseType) {
		return testRestTemplate.exchange(API_USERS, HttpMethod.GET, null, responseType);
	}

	private <T> ResponseEntity<T> getUsers(final String path, final ParameterizedTypeReference<T> responseType) {
		return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
	}

	private <T> ResponseEntity<T> getUser(final String username, final Class<T> responseType) {
		final String path = API_USERS + "/" + username;
		return testRestTemplate.getForEntity(path, responseType);
	}

	private <T> ResponseEntity<T> putUser(final long id, final HttpEntity<?> requestEntity,
			final Class<T> responseType) {
		final String path = API_USERS + "/" + id;
		return testRestTemplate.exchange(path, HttpMethod.PUT, requestEntity, responseType);
	}

	private void authenticate(final String username) {
		testRestTemplate.getRestTemplate().getInterceptors()
				.add(new BasicAuthenticationInterceptor(username, "P4ssword"));
	}

}
