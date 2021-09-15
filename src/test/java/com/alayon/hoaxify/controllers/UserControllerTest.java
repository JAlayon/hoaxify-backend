package com.alayon.hoaxify.controllers;

import static com.alayon.hoaxify.utils.TestUtil.authenticate;
import static com.alayon.hoaxify.utils.TestUtil.getValidUserForRequest;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.alayon.hoaxify.user.dto.UserRequest;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.alayon.hoaxify.commons.GenericResponse;
import com.alayon.hoaxify.config.AppConfiguration;
import com.alayon.hoaxify.error.ApiError;
import com.alayon.hoaxify.user.TestPage;
import com.alayon.hoaxify.user.model.User;
import com.alayon.hoaxify.user.repository.UserRepository;
import com.alayon.hoaxify.user.service.UserService;
import com.alayon.hoaxify.user.dto.UserDto;
import com.alayon.hoaxify.user.dto.UserUpdateDto;
import com.alayon.hoaxify.utils.TestUtil;

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
		final UserRequest userRequest = TestUtil.getValidUserForRequest();
		final ResponseEntity<?> response = postSignup(userRequest, null);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	public void postUser_whenUserIsValid_userSavedToDatabase() {
		final UserRequest userRequest = TestUtil.getValidUserForRequest();
		testRestTemplate.postForEntity(API_USERS, userRequest, null);
		assertThat(userRepository.count()).isEqualTo(1);
	}

	@Test
	public void postUser_whenUserIsValid_receiveSuccessMessage() {
		final UserRequest userRequest = TestUtil.getValidUserForRequest();
		final ResponseEntity<GenericResponse> response = postSignup(userRequest, GenericResponse.class);
		assertThat(response.getBody().getMessage()).isNotNull();
	}

	@Test
	public void postUser_whenUserIsValid_passwordIsHashedInDb() {
		final UserRequest userRequest = TestUtil.getValidUserForRequest();
		testRestTemplate.postForEntity(API_USERS, userRequest, null);
		final List<User> users = userRepository.findAll();
		final User userInDb = users.get(0);
		assertThat(userInDb.getPassword()).isNotEqualTo(userRequest.getPassword());
	}

	@Test
	public void postUser_whenUserHasNullUsername_receiveBadRequest() {
		final UserRequest userRequest = TestUtil.getValidUserForRequest();
		userRequest.setUsername(null);
		final ResponseEntity<Object> response = postSignup(userRequest, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void postUser_whenUserHasNullDisplayName_receiveBadRequest() {
		final UserRequest userRequest = TestUtil.getValidUserForRequest();
		userRequest.setDisplayname(null);
		final ResponseEntity<Object> response = postSignup(userRequest, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void postUser_whenUserHasNullPassword_receiveBadRequest() {
		final UserRequest userRequest = TestUtil.getValidUserForRequest();
		userRequest.setPassword(null);
		final ResponseEntity<Object> response = postSignup(userRequest, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void postUser_whenUserHasUsernameWithLessThanRequired_receiveBadRequest() {
		final UserRequest userRequest = TestUtil.getValidUserForRequest();
		userRequest.setUsername("abc");
		final ResponseEntity<Object> response = postSignup(userRequest, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void postUser_whenUserHasDisplayNameWithLessThanRequired_receiveBadRequest() {
		final UserRequest userRequest = TestUtil.getValidUserForRequest();
		userRequest.setDisplayname("abc");
		final ResponseEntity<Object> response = postSignup(userRequest, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void postUser_whenUserHasPasswordWithLessThanRequired_receiveBadRequest() {
		final UserRequest userRequest = TestUtil.getValidUserForRequest();
		userRequest.setPassword("P4ssw");
		final ResponseEntity<Object> response = postSignup(userRequest, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void postUser_whenUserHasUsernameExceedsTheLengthLimit_receiveBadRequest() {
		final UserRequest userRequest = TestUtil.getValidUserForRequest();
		final String valueOf256Chars = IntStream.rangeClosed(1, 256).mapToObj(x -> "a").collect(Collectors.joining());
		userRequest.setUsername(valueOf256Chars);
		final ResponseEntity<Object> response = postSignup(userRequest, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void postUser_whenUserHasDisplayNameExceedsTheLengthLimit_receiveBadRequest() {
		final UserRequest userRequest = TestUtil.getValidUserForRequest();
		final String valueOf256Chars = IntStream.rangeClosed(1, 256).mapToObj(x -> "a").collect(Collectors.joining());
		userRequest.setDisplayname(valueOf256Chars);
		final ResponseEntity<Object> response = postSignup(userRequest, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void postUser_whenUserHasPasswordExceedsTheLengthLimit_receiveBadRequest() {
		final UserRequest userRequest = TestUtil.getValidUserForRequest();
		final String valueOf256Chars = IntStream.rangeClosed(1, 256).mapToObj(x -> "a").collect(Collectors.joining());
		userRequest.setPassword(valueOf256Chars + "A1");
		final ResponseEntity<Object> response = postSignup(userRequest, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void postUser_whenUserHasPasswordWithAllLowercase_receiveBadRequest() {
		final UserRequest userRequest = TestUtil.getValidUserForRequest();
		userRequest.setPassword("alllowercase");
		final ResponseEntity<Object> response = postSignup(userRequest, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void postUser_whenUserHasPasswordWithAllUppercase_receiveBadRequest() {
		final UserRequest userRequest = TestUtil.getValidUserForRequest();
		userRequest.setPassword("ALLUPPERCASE");
		final ResponseEntity<Object> response = postSignup(userRequest, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void postUser_whenUserHasPasswordWithAllNumbers_receiveBadRequest() {
		final UserRequest userRequest = TestUtil.getValidUserForRequest();
		userRequest.setPassword("123456789");
		final ResponseEntity<Object> response = postSignup(userRequest, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void postUser_whenUserHasNotBody_receiveApiError() {
		final UserRequest userRequest = new UserRequest();
		final ResponseEntity<ApiError> response = postSignup(userRequest, ApiError.class);
		assertThat(response.getBody().getUrl()).isEqualTo(API_USERS);
	}

	@Test
	public void postUser_whenUserHasNotBody_receiveApiErrorWithValidationErrors(){
		final UserRequest userRequest = new UserRequest();
		final ResponseEntity<ApiError> response = postSignup(userRequest, ApiError.class);
		assertThat(response.getBody().getValidationErrors().size()).isEqualTo(3);
	}

	@Test
	public void postUser_whenUserHasNullUsername_receiveMessageOfNullErrorForUsername() {
		final UserRequest userRequest = TestUtil.getValidUserForRequest();
		userRequest.setUsername(null);
		final ResponseEntity<ApiError> response = postSignup(userRequest, ApiError.class);
		final Map<String, String> validationErrors = response.getBody().getValidationErrors();
		assertThat(validationErrors.get("username")).isEqualTo("Username cannot be null");
	}

	@Test
	public void postUser_whenUserHasNullPassword_receiveMessageOfNullError() {
		final UserRequest userRequest = TestUtil.getValidUserForRequest();
		userRequest.setPassword(null);
		final ResponseEntity<ApiError> response = postSignup(userRequest, ApiError.class);
		final Map<String, String> validationErrors = response.getBody().getValidationErrors();
		assertThat(validationErrors.get("password")).isEqualTo("Password cannot be null");
	}

	@Test
	public void postUser_whenUserHasInvalidLengthUsername_receiveGenericMessageOfSizeError() {
		final UserRequest userRequest = TestUtil.getValidUserForRequest();
		userRequest.setUsername("abc");
		final ResponseEntity<ApiError> response = postSignup(userRequest, ApiError.class);
		final Map<String, String> validationErrors = response.getBody().getValidationErrors();
		assertThat(validationErrors.get("username")).isEqualTo("It must have minimum 4 and maximum 255 characters");
	}

	@Test
	public void postUser_whenUserHasInvalidPasswordPattern_receiveMessageOfPasswordPatternError() {
		final UserRequest userRequest = TestUtil.getValidUserForRequest();
		userRequest.setPassword("alllowercase");
		final ResponseEntity<ApiError> response = postSignup(userRequest, ApiError.class);
		final Map<String, String> validationErrors = response.getBody().getValidationErrors();
		assertThat(validationErrors.get("password"))
				.isEqualTo("Password must have at least one uppercase, one lowercase letter and one number");
	}

	@Test
	public void postUser_whenAnotherUserHasSameUsername_receiveBadRequest() {
		userRepository.save(TestUtil.getValidUser());
		final UserRequest userRequest = TestUtil.getValidUserForRequest();
		final ResponseEntity<?> response = postSignup(userRequest, null);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void postUser_whenAnotherUserHasSameUsername_receiveMessageOfDuplicateUsername() {
		userRepository.save(TestUtil.getValidUser());
		final UserRequest userRequest = TestUtil.getValidUserForRequest();
		final ResponseEntity<ApiError> response = postSignup(userRequest, ApiError.class);
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
		userRepository.save(TestUtil.getValidUser());
		final ResponseEntity<TestPage<?>> response = getUsers(new ParameterizedTypeReference<TestPage<?>>() {
		});
		assertThat(response.getBody().getNumberOfElements()).isEqualTo(1);
	}

	@Test
	public void getUsers_whenThereIsAnUserInDb_receiveUserWithoutPassword() {
		userRepository.save(TestUtil.getValidUser());
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
		userService.save(getValidUserForRequest("user1"));
		userService.save(getValidUserForRequest("user2"));
		userService.save(getValidUserForRequest("user3"));
		authenticate(testRestTemplate, "user1");
		final ResponseEntity<TestPage<?>> response = getUsers(new ParameterizedTypeReference<TestPage<?>>() {
		});
		assertThat(response.getBody().getTotalElements()).isEqualTo(2);
	}

	@Test
	public void getUserByUsername_whenUserExists_receiveOk() {
		final String username = "test-user";
		userService.save(getValidUserForRequest(username));
		final ResponseEntity<?> response = getUser(username, null);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	public void getUserByUsername_whenUserExists_receivUserWithoutPassword() {
		final String username = "test-user";
		userService.save(getValidUserForRequest(username));
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
		final User user = userService.save(TestUtil.getValidUserForRequest("user1"));
		authenticate(testRestTemplate, user.getUsername());
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
		final User user = userService.save(TestUtil.getValidUserForRequest("user1"));
		authenticate(testRestTemplate, user.getUsername());
		final long anotherUserId = user.getId() + 123;
		final ResponseEntity<ApiError> response = putUser(anotherUserId, null, ApiError.class);
		assertThat(response.getBody().getUrl()).contains("users/" + anotherUserId);
	}

	@Test
	public void putUser_whenValidRequestBodyFromAuthorizedUser_receiveOk() {
		final User user = userService.save(TestUtil.getValidUserForRequest("user1"));
		authenticate(testRestTemplate, user.getUsername());

		final HttpEntity<UserUpdateDto> requestEntity = new HttpEntity<>(TestUtil.getValidUserUpdate());
		final ResponseEntity<?> response = putUser(user.getId(), requestEntity, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	public void putUser_whenValidRequestBodyFromAuthorizedUser_displayNameUpdated() {
		final User user = userService.save(TestUtil.getValidUserForRequest("user1"));
		authenticate(testRestTemplate, user.getUsername());

		final HttpEntity<UserUpdateDto> requestEntity = new HttpEntity<>(TestUtil.getValidUserUpdate());
		putUser(user.getId(), requestEntity, Object.class);

		final User userInDb = userRepository.findByUsername("user1");
		assertThat(userInDb.getDisplayname()).isEqualTo(TestUtil.getValidUserUpdate().getDisplayName());
	}

	@Test
	public void putUser_whenValidRequestBodyFromAuthorizedUser_receiveUserDtoWithUpdatedDisplayName() {
		final User user = userService.save(TestUtil.getValidUserForRequest("user1"));
		authenticate(testRestTemplate, user.getUsername());

		final HttpEntity<UserUpdateDto> requestEntity = new HttpEntity<>(TestUtil.getValidUserUpdate());
		final ResponseEntity<UserUpdateDto> response = putUser(user.getId(), requestEntity, UserUpdateDto.class);

		assertThat(response.getBody().getDisplayName()).isEqualTo(TestUtil.getValidUserUpdate().getDisplayName());
	}

	@Test
	public void putUser_whenValidRequestBodyWithSupportedImageFromAuthorizedUser_receiveUserDtoWithRandomImageName()
			throws IOException {
		final User user = userService.save(TestUtil.getValidUserForRequest("user1"));
		authenticate(testRestTemplate, user.getUsername());

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
		final User user = userService.save(TestUtil.getValidUserForRequest("user1"));
		authenticate(testRestTemplate, user.getUsername());

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

	@Test
	public void putUser_whenInvalidRequestBodyWithNullDisplayNameFromAuthorizedUser_receiveBadRequest()
			throws IOException {
		final User user = userService.save(TestUtil.getValidUserForRequest("user1"));
		authenticate(testRestTemplate, user.getUsername());

		final UserUpdateDto userUpdateDto = new UserUpdateDto();

		final HttpEntity<UserUpdateDto> requestEntity = new HttpEntity<>(userUpdateDto);
		final ResponseEntity<Object> response = putUser(user.getId(), requestEntity, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

	}

	@Test
	public void putUser_whenInvalidRequestBodyWithLessThanMinimumSizeDisplayNameFromAuthorizedUser_receiveBadRequest()
			throws IOException {
		final User user = userService.save(TestUtil.getValidUserForRequest("user1"));
		authenticate(testRestTemplate, user.getUsername());

		final UserUpdateDto userUpdateDto = new UserUpdateDto();
		userUpdateDto.setDisplayName("abc");

		final HttpEntity<UserUpdateDto> requestEntity = new HttpEntity<>(userUpdateDto);
		final ResponseEntity<Object> response = putUser(user.getId(), requestEntity, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

	}

	@Test
	public void putUser_whenInvalidRequestBodyWithMoreThanMaxSizeDisplayNameFromAuthorizedUser_receiveBadRequest()
			throws IOException {
		final User user = userService.save(TestUtil.getValidUserForRequest("user1"));
		authenticate(testRestTemplate, user.getUsername());

		final UserUpdateDto userUpdateDto = new UserUpdateDto();
		final String valueOf256Chars = IntStream.rangeClosed(1, 256).mapToObj(x -> "a").collect(Collectors.joining());
		userUpdateDto.setDisplayName(valueOf256Chars);

		final HttpEntity<UserUpdateDto> requestEntity = new HttpEntity<>(userUpdateDto);
		final ResponseEntity<Object> response = putUser(user.getId(), requestEntity, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

	}

	@Test
	public void putUser_whithValidRequestBodyWithJPGImageFromAuthorizedUser_receiveOk() throws IOException {
		final User user = userService.save(TestUtil.getValidUserForRequest("user1"));
		authenticate(testRestTemplate, user.getUsername());

		final UserUpdateDto userUpdateDto = TestUtil.getValidUserUpdate();
		final String imageString = readFileToBase64("test-jpg.jpg");

		userUpdateDto.setImage(imageString);

		final HttpEntity<UserUpdateDto> requestEntity = new HttpEntity<>(userUpdateDto);
		final ResponseEntity<UserDto> response = putUser(user.getId(), requestEntity, UserDto.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

	}

	@Test
	public void putUser_whithValidRequestBodyWithGIFImageFromAuthorizedUser_receiveBadRequest() throws IOException {
		final User user = userService.save(TestUtil.getValidUserForRequest("user1"));
		authenticate(testRestTemplate, user.getUsername());

		final UserUpdateDto userUpdateDto = TestUtil.getValidUserUpdate();
		final String imageString = readFileToBase64("test-gif.gif");

		userUpdateDto.setImage(imageString);

		final HttpEntity<UserUpdateDto> requestEntity = new HttpEntity<>(userUpdateDto);
		final ResponseEntity<Object> response = putUser(user.getId(), requestEntity, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

	}

	@Test
	public void putUser_whithValidRequestBodyWithTXTImageFromAuthorizedUser_receiveValidationErrorForProfileImage()
			throws IOException {
		final User user = userService.save(TestUtil.getValidUserForRequest("user1"));
		authenticate(testRestTemplate, user.getUsername());

		final UserUpdateDto userUpdateDto = TestUtil.getValidUserUpdate();
		final String imageString = readFileToBase64("test-txt.txt");

		userUpdateDto.setImage(imageString);

		final HttpEntity<UserUpdateDto> requestEntity = new HttpEntity<>(userUpdateDto);
		final ResponseEntity<ApiError> response = putUser(user.getId(), requestEntity, ApiError.class);
		final Map<String, String> validationErrors = response.getBody().getValidationErrors();
		assertThat(validationErrors.get("image")).isEqualTo("Only PNG and JPG files are allowed");

	}

	@Test
	public void putUser_whithValidRequestBodyWithJPGImageForUserWhoHasImage_removesOldImageFromStorage()
			throws IOException {
		final User user = userService.save(TestUtil.getValidUserForRequest("user1"));
		authenticate(testRestTemplate, user.getUsername());

		final UserUpdateDto userUpdateDto = TestUtil.getValidUserUpdate();
		final String imageString = readFileToBase64("test-jpg.jpg");

		userUpdateDto.setImage(imageString);

		final HttpEntity<UserUpdateDto> requestEntity = new HttpEntity<>(userUpdateDto);
		final ResponseEntity<UserDto> response = putUser(user.getId(), requestEntity, UserDto.class);

		putUser(user.getId(), requestEntity, UserDto.class);
		final String storedImageName = response.getBody().getImage();
		final String profilePicturePath = appConfig.getFullProfileImagePath() + "/" + storedImageName;
		final File storedImageFile = new File(profilePicturePath);

		assertThat(storedImageFile.exists()).isFalse();

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

}
