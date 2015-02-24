package com.sandp.spratings.portlet.util;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.Contact;
import com.liferay.portal.model.Country;
import com.liferay.portal.model.ListType;
import com.liferay.portal.model.ListTypeConstants;
import com.liferay.portal.model.Role;
import com.liferay.portal.security.auth.PrincipalThreadLocal;
import com.liferay.portal.security.permission.PermissionChecker;
import com.liferay.portal.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.security.permission.PermissionThreadLocal;
import com.liferay.portal.service.AddressLocalServiceUtil;
import com.liferay.portal.service.CountryServiceUtil;
import com.liferay.portal.service.ListTypeServiceUtil;
import com.liferay.portal.service.RoleLocalServiceUtil;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.expando.model.ExpandoTableConstants;
import com.liferay.portlet.expando.service.ExpandoValueLocalServiceUtil;
import com.liferay.util.portlet.PortletProps;
import com.sandp.spratings.portlet.tokenutils.HeaderHandlerResolver;
import com.sandp.spratings.portlet.valueobjects.UserApplications;
import com.sandp.spratings.portlet.valueobjects.UserMetaData;
import com.standardandpoors.integration.schema.idm.Address;
import com.standardandpoors.integration.schema.idm.Application;
import com.standardandpoors.integration.schema.idm.IdmService;
import com.standardandpoors.integration.schema.idm.IdmWebServicePort;
import com.standardandpoors.integration.schema.idm.Profile;
import com.standardandpoors.integration.schema.idm.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.RenderRequest;
import javax.portlet.ResourceRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.BindingProvider;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Handles all common methods in article delegators.
 * 
 * @author ranjith_narahari
 * 
 */
@Component
public class UserUtil {

	/**
	 * Log variable.
	 */
	private static Log log = LogFactoryUtil.getLog(UserUtil.class);

	

	/**
	 * This method validates Mail contact preferences.
	 * 
	 * @param request
	 *            ActionRequest
	 * @return boolean
	 */
	public boolean validateMail(ActionRequest request) {

		log.debug("Validating mail contact preferences");
		String address1 = ParamUtil.getString(request, UserConstants.ADDRESS1);
		String city = ParamUtil.getString(request, UserConstants.CITY);
		String postalCode = ParamUtil.getString(request,
				UserConstants.POSTALCODE);
		String contactMail = ParamUtil.getString(request,
				UserConstants.CONTACT_MAIL);

		log.debug("address1 " + address1);
		log.debug("city " + city);
		log.debug("postalCode " + postalCode);
		log.debug("contactMail " + contactMail);

		boolean isMailValidated = true;

		if (!StringUtils.isEmpty(contactMail)) {
			if (StringUtils.isEmpty(address1.trim())) {
				isMailValidated = false;
			}
			if (StringUtils.isEmpty(city.trim())) {
				isMailValidated = false;
			}
			if (StringUtils.isEmpty(postalCode.trim())) {
				isMailValidated = false;
			}
		}
		log.debug("Mail contact preferences validation : " + isMailValidated);
		return isMailValidated;
	}

	/**
	 * This method validates Phone contact preferences.
	 * 
	 * @param request
	 *            ActionRequest
	 * @return boolean
	 */
	public boolean validatePhone(ActionRequest request) {

		log.debug("Validating phone contact preference");
		String phone = ParamUtil.getString(request, UserConstants.PHONE);
		String contactPhone = ParamUtil.getString(request,
				UserConstants.CONTACT_PHONE);

		log.debug("phone " + phone);
		log.debug("contactPhone " + contactPhone);

		boolean isPhoneValidated = true;

		Pattern phonePattern = Pattern.compile(UserConstants.PHONE_PATTERN);
		Matcher phoneMatcher = phonePattern.matcher(phone);

		if (!StringUtils.isEmpty(contactPhone)) {
			if (!phoneMatcher.matches()
					|| phone.trim().length() < UserConstants.PHONE_NUMBER_LENGTH) {
				isPhoneValidated = false;
			}
		}
		log.debug("Phone contact preference validation : " + isPhoneValidated);
		return isPhoneValidated;
	}

	/**
	 * This method update the user details in Liferay if User exist.
	 * 
	 * @param liferayUser
	 *            User
	 * @param request
	 *            ActionRequest
	 * @return user
	 */
	public com.liferay.portal.model.User updateUser(
			com.liferay.portal.model.User liferayUser, ActionRequest request) {

		log.debug("Updating User in Liferay");

		try {
			liferayUser.setFirstName(ParamUtil.getString(request,
					UserConstants.FNAME));
			liferayUser.setLastName(ParamUtil.getString(request,
					UserConstants.LNAME));
			liferayUser.setAgreedToTermsOfUse(Boolean.TRUE);
			liferayUser.setEmailAddressVerified(Boolean.TRUE);

			liferayUser = UserLocalServiceUtil.updateUser(liferayUser);
			log.debug("User Updated in liferay");
		} catch (SystemException e) {
			log.error("Exception occurred while updating User data in liferay :: "
					+ e.getMessage());
		}
		return liferayUser;
	}

	
	/**
	 * This method will returns Liferay user object.
	 * 
	 * @param request
	 *            ActionRequest
	 * @return User
	 */
	public com.liferay.portal.model.User getLiferayUser(ActionRequest request) {
		com.liferay.portal.model.User liferayUser = null;

		ThemeDisplay themeDisplay = (ThemeDisplay) request
				.getAttribute(WebKeys.THEME_DISPLAY);

		try {
			liferayUser = UserLocalServiceUtil.getUserByEmailAddress(
					themeDisplay.getCompanyId(), themeDisplay.getUser()
							.getEmailAddress());
		} catch (PortalException e) {
			log.error("PortalException occurred while verifying duplicate IDs in Liferay :: "
					+ e.getMessage());
			return liferayUser;
		} catch (SystemException e) {
			log.error("SystemException occurred while verifying duplicate IDs in Liferay :: "
					+ e.getMessage());
			return liferayUser;
		}

		return liferayUser;
	}

	

	/**
	 * This method formats the empty strings.
	 * 
	 * @param input
	 *            String
	 * @return String
	 */
	public String formatEmptyString(String input) {

		String output;
		if (input == null || StringPool.BLANK.equals(input.trim())) {
			output = UserConstants.ADDRESS_NOT_APPLICABLE;
		} else {
			output = input;
		}

		return output;
	}

	/**
	 * This method formats input strings.
	 * 
	 * @param input
	 *            String
	 * @return String
	 */
	public String formatNAString(String input) {

		String output;
		if (input == null
				|| UserConstants.ADDRESS_NOT_APPLICABLE.equals(input.trim())) {
			output = StringPool.BLANK;
		} else {
			output = input;
		}

		return output;
	}

	/**
	 * This method sets the Cookies for User country and AWD.
	 * 
	 * @param request
	 *            ActionRequest
	 * @param response
	 *            ActionResponse
	 */
	public void setCookies(ActionRequest request, ActionResponse response) {
		log.debug("Seting the cookie values for country and awd");

		String country = ParamUtil.getString(request, UserConstants.COUNTRY);
		String awd = ParamUtil.getString(request, UserConstants.AWS_SETTING);
		HttpServletResponse res = PortalUtil.getHttpServletResponse(response);
		Cookie country_cookie = new Cookie(UserConstants.USER_MAIL_COUNTRY, country);
		Cookie awd_cookie = new Cookie(UserConstants.WHOLESALER_FLAG, awd);
		res.addCookie(country_cookie);
		res.addCookie(awd_cookie);

		log.debug("Cookie set completed ");
	}

	/**
	 * This method will retrieve the User Custom field value for the input
	 * custom field name.
	 * 
	 * @param liferayUser
	 *            User
	 * @param customFieldName
	 *            Stirng
	 * @return Serializable
	 */
	public Serializable getUserCustomFieldValue(
			com.liferay.portal.model.User liferayUser, String customFieldName) {
		Serializable customFieldValue = StringPool.BLANK;
		try {
			// permission checker
			PrincipalThreadLocal.setName(liferayUser.getUserId());
			log.debug("Adding Permission Checker");
			PermissionChecker permissionChecker = PermissionCheckerFactoryUtil
					.create(liferayUser);
			log.debug("Added Permission Checker");
			PermissionThreadLocal.setPermissionChecker(permissionChecker);

			customFieldValue = (Serializable) liferayUser.getExpandoBridge()
					.getAttribute(customFieldName);
			log.debug(customFieldName + "custom field value :: "
					+ customFieldValue);
		} catch (Exception e) {
			log.error("Exception occurred while fetching custom filed value", e);
			return StringPool.BLANK;
		}
		return customFieldValue == null ? StringPool.BLANK : customFieldValue;
	}
	
	/**
	 * Adds the user specific custom field for the given name and value
	 * @param user
	 * @param name
	 * @param value
	 * @throws PortalException
	 * @throws SystemException
	 */
	public void saveUserCustomAttribute(com.liferay.portal.model.User user,String name,String value) throws PortalException, SystemException{
		ExpandoValueLocalServiceUtil.addValue(user.getCompanyId(),
				com.liferay.portal.model.User.class.getName(),
				ExpandoTableConstants.DEFAULT_TABLE_NAME,
				name,
				user.getUserId(), value);
		log.debug("custom attribute saved :::: name :" +name +" value ::: "+value);
	}

}
