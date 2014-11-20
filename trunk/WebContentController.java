package com.ness.webcontent;

import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.ServiceContextFactory;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.journal.model.JournalArticle;
import com.liferay.portlet.journal.model.JournalArticleConstants;
import com.liferay.portlet.journal.model.JournalStructure;
import com.liferay.portlet.journal.model.JournalTemplate;
import com.liferay.portlet.journal.model.JournalTemplateConstants;
import com.liferay.portlet.journal.service.JournalArticleLocalServiceUtil;
import com.liferay.portlet.journal.service.JournalStructureLocalServiceUtil;
import com.liferay.portlet.journal.service.JournalTemplateLocalServiceUtil;
import com.liferay.util.bridges.mvc.MVCPortlet;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

/**
 * Portlet implementation class WebContentController
 */
public class WebContentController extends MVCPortlet {

	private String articleTitle = "Article Title ";
	private String articleContent = "<div>Article Content <b> Hello Ness </b> </div>";
	private String structureTitle = "Structure Title ";
	private String structureDesc = "Structure Description ";
	private String templateTitle = "Template Title ";
	private String templateDesc = "Template Description ";
	
	
	@Override
	public void render(RenderRequest request, RenderResponse response)
			throws PortletException, IOException {
		System.out.println("Render is called.>>>>>>>>>>>");
		try {
			JournalArticle journalArticle = null;
			
			long userId = PortalUtil.getUserId(request);
			ThemeDisplay themeDisplay= (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
			long portletGroupId= themeDisplay.getPortletGroupId();
			ServiceContext serviceContext = ServiceContextFactory.getInstance(JournalArticle.class.getName(), request);

		
			Map<Locale, String> titleMap = new HashMap<Locale, String>();
			setLocalizedValue(titleMap, articleTitle+new Date(System.currentTimeMillis()));
			
			//Creating journalStructure and journalTemplate
			JournalStructure journalStructure = createJournalStructure(userId, portletGroupId,request);
			JournalTemplate journalTemplate = addJournalTemplate(userId, portletGroupId, journalStructure.getStructureId(),request);

			Calendar displayCalendar = Calendar.getInstance();
			Calendar expirationCalc = new GregorianCalendar(2020, 04, 21);
			Calendar reviewCalc = new GregorianCalendar(2014, 11, 21);
			
			journalArticle = JournalArticleLocalServiceUtil.addArticle(userId, portletGroupId, 0, 0, articleTitle.replaceAll(" ", "_"), true, JournalArticleConstants.VERSION_DEFAULT, titleMap, null,
					articleContent, "general", journalStructure.getStructureId(), journalTemplate.getTemplateId(), StringPool.BLANK, displayCalendar.get(Calendar.MONTH)+1, displayCalendar.get(Calendar.DAY_OF_MONTH),
					displayCalendar.get(Calendar.YEAR), displayCalendar.get(Calendar.HOUR), displayCalendar.get(Calendar.MINUTE), 
					expirationCalc.get(Calendar.MONTH)+1, expirationCalc.get(Calendar.DAY_OF_MONTH),expirationCalc.get(Calendar.YEAR), 
					expirationCalc.get(Calendar.HOUR), expirationCalc.get(Calendar.MINUTE), true, reviewCalc.get(Calendar.MONTH)+1, 
					reviewCalc.get(Calendar.DAY_OF_MONTH),reviewCalc.get(Calendar.YEAR), reviewCalc.get(Calendar.HOUR), reviewCalc.get(Calendar.MINUTE),
					true, true, false, StringPool.BLANK, null,
					null, StringPool.BLANK, serviceContext);
			
			System.out.println("Journal Article Created Successfully>>>>>>>>>>>>>>>>> "+journalArticle.getArticleId());
		} catch (Exception e) {
			System.out.println("Exception occured>>>>>>>>>"+e);
			e.printStackTrace();
		} 
		super.render(request, response);
	}
	
	private JournalStructure createJournalStructure(long userId,long groupId,RenderRequest request) throws Exception{
		JournalStructure journalStructure = null;
		Map<Locale, String> titleMap = new HashMap<Locale, String>();
		Map<Locale, String> descMap = new HashMap<Locale, String>();
		setLocalizedValue(titleMap, structureTitle+new Date(System.currentTimeMillis()));
		setLocalizedValue(descMap, structureDesc);
		ServiceContext serviceContext = ServiceContextFactory.getInstance(JournalStructure.class.getName(), request);

		//We can save the below xsd data in a file and we can read the file and convert to string.. 
		//InputStream inputStream = new FileInputStream(new File(path+"/WEB-INF/xsd/articleStructure.xml"));
		//String xsd = IOUtils.toString(inputStream);
		
		//The below structure is having 2 dynamic elements (image(type is image) and content(type is textArea))
		//image dynamic element is having 2 child elements (width(type is text) and height(type is text)) 
		String xsd = "<?xml version='1.0'?><root><dynamic-element name='image' type='image' index-type='' repeatable='false'>"+
				 " <meta-data> "+
				 "<entry name='displayAsTooltip'><![CDATA[false]]></entry> "+
				 "<entry name='required'><![CDATA[false]]></entry> "+
				 "<entry name='instructions'><![CDATA[]]></entry> "+
				 "<entry name='label'><![CDATA[image]]></entry> "+
				 "<entry name='predefinedValue'><![CDATA[]]></entry> "+
				 "</meta-data> "+
				 "<dynamic-element name='height' type='text' index-type='' repeatable='false'> "+
				 "<meta-data> "+
				 "	<entry name='displayAsTooltip'><![CDATA[false]]></entry> "+
				 "	<entry name='required'><![CDATA[false]]></entry> "+
				 "	<entry name='instructions'><![CDATA[]]></entry> "+
				 "<entry name='label'><![CDATA[height]]></entry> "+
				 "	<entry name='predefinedValue'><![CDATA[]]></entry> "+
				 "</meta-data> "+
				 "</dynamic-element> "+
				 "<dynamic-element name='width' type='text' index-type='' repeatable='false'> "+
				 "<meta-data> "+
				 "	<entry name='displayAsTooltip'><![CDATA[false]]></entry> "+
				 "<entry name='required'><![CDATA[false]]></entry> "+
				 "<entry name='instructions'><![CDATA[]]></entry> "+
				 "<entry name='label'><![CDATA[width]]></entry> "+
				 "<entry name='predefinedValue'><![CDATA[]]></entry> "+
				 "</meta-data> "+
				 "</dynamic-element> "+
				 "</dynamic-element> "+
				 "<dynamic-element name='content' type='text_area' index-type='' repeatable='false'/> "+
				 "</root>";
		journalStructure = JournalStructureLocalServiceUtil.addStructure(userId, groupId, "Sample structureId"+System.currentTimeMillis(), true, null, titleMap, descMap, xsd, serviceContext);
		System.out.println("Structure is created Successfully>>>>>>"+journalStructure.getStructureId());
		return journalStructure;
	}
	
	private JournalTemplate addJournalTemplate(long userId,long groupId,String structureId,RenderRequest request)throws Exception{
		JournalTemplate journalTemplate = null;
		Map<Locale, String> titleMap = new HashMap<Locale, String>();
		Map<Locale, String> descMap = new HashMap<Locale, String>();
		setLocalizedValue(titleMap, templateTitle+new Date(System.currentTimeMillis()));
		setLocalizedValue(descMap, templateDesc);
		ServiceContext serviceContext = ServiceContextFactory.getInstance(JournalTemplate.class.getName(), request);
		
		//We can save the below vm data in a file and we can read the file and convert to string.. 
		//InputStream inputStream = new FileInputStream(new File(path+"/WEB-INF/template/articleTemplate.vm"));
		//String vm = IOUtils.toString(inputStream);
		
		// The below vm shows the template design
		String vm = "<table width='60%' border='1'><tr><td width='40%'>Article Image </td><td width='60%'>" +
				"<img src='$image.getData()' height='$image.height.getData()' width='$image.width.getData()'></td></tr></table>" +
				"<div><b>Detailed description </b> <p>$content.getData()</div>";
		
		journalTemplate = JournalTemplateLocalServiceUtil.addTemplate(userId, groupId, "Sample templateId"+System.currentTimeMillis(), true, structureId, titleMap, descMap, vm, true, JournalTemplateConstants.LANG_TYPE_VM, false, false, null, null, serviceContext);
		System.out.println("Template is created Successfully>>>>>>"+journalTemplate.getTemplateId());
		return journalTemplate;
	}

	private void setLocalizedValue(final Map<Locale, String> map, final String value) {
		Locale locale = LocaleUtil.getDefault();

		map.put(locale, value);

		if (!locale.equals(Locale.US)) {
			map.put(Locale.US, value);
		}
	}

}
