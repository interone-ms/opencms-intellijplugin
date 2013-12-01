package com.mediaworx.opencms.intellijconnector;

import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.opencms.db.CmsPublishList;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.publish.CmsPublishManager;
import org.opencms.report.CmsLogReport;
import org.opencms.report.I_CmsReport;

import javax.servlet.ServletOutputStream;
import javax.servlet.jsp.PageContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class OpenCmsIntelliJConnector {

	private static final Log LOG = LogFactory.getLog(OpenCmsIntelliJConnector.class);

	private static final String ACTION_MODULEMANIFESTS = "moduleManifests";
	private static final String ACTION_RESOURCEINFOS = "resourceInfos";
	private static final String ACTION_PUBLISH = "publishResources";

	private ServletOutputStream out;
	private CmsObject cmsObject;
	private MetaXmlHelper xmlHelper;

	private JSONParser jsonParser;

	String action;
	String params;

	public OpenCmsIntelliJConnector(PageContext pageContext) throws IOException {
		out = pageContext.getResponse().getOutputStream();

		CmsFlexController flexController = CmsFlexController.getController(pageContext.getRequest());
		cmsObject = flexController.getCmsObject();
		xmlHelper = new MetaXmlHelper(cmsObject);
		jsonParser = new JSONParser();

		action = pageContext.getRequest().getParameter("action");
		params = pageContext.getRequest().getParameter("params");

		String user = pageContext.getRequest().getParameter("user");
		String password = pageContext.getRequest().getParameter("password");

		login(user, password);
	}

	private void login(String user, String password) {
		boolean isUserLoggedIn = !cmsObject.getRequestContext().getCurrentUser().isGuestUser();

		if (!isUserLoggedIn) {
			try {
				cmsObject.loginUser(user, password);
				CmsProject cmsproject = cmsObject.readProject("Offline");
				cmsObject.getRequestContext().setCurrentProject(cmsproject);
				cmsObject.getRequestContext().setSiteRoot("/");
			}
			catch (CmsException e) {
				LOG.error("the user " + user + " can't be logged in", e);
			}
		}
	}


	public void streamResponse() {
		if (ACTION_MODULEMANIFESTS.equals(action)) {
			streamModuleManifestsOrResourceInfos(true);
		}
		else if (ACTION_RESOURCEINFOS.equals(action)) {
			streamModuleManifestsOrResourceInfos(false);
		}
		else if (ACTION_PUBLISH.equals(action)) {
			publishResources();
		}
	}

	@SuppressWarnings("unchecked")
	private void streamModuleManifestsOrResourceInfos(boolean isModuleManifest) {
		String[] ids = getStringArrayFromJSON(params);
		if (ids == null) {
			return;
		}

		JSONArray out = new JSONArray();
		for (String id : ids) {
			String xml;
			if (isModuleManifest) {
				try {
					xml = xmlHelper.getModuleManifestStub(id);
				}
				catch (IllegalArgumentException e) {
					LOG.error(id + " is not a valid module name");
					continue;
				}
			}
			else {
				if (!cmsObject.existsResource(id)) {
					continue;
				}
				xml = xmlHelper.getResourceInfo(id);
			}
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("id", id);
			jsonObject.put("xml", xml);
			out.add(jsonObject);
		}
		println(out.toJSONString());
	}

	private void publishResources() {
		String[] resourcePaths = getStringArrayFromJSON(params);

		List<CmsResource> publishResources = new ArrayList<CmsResource>(resourcePaths.length);
		boolean hasWarnings = false;
		StringBuilder warnings = new StringBuilder();

		for (String resourcePath : resourcePaths) {
			if (cmsObject.existsResource(resourcePath)) {
				CmsResource resource;
				try {
					resource = cmsObject.readResource(resourcePath);
				}
				catch (CmsException e) {
					String message = resourcePath + " could not be read from the VFS";
					warnings.append(message).append("\n");
					LOG.warn(message, e);
					hasWarnings = true;
					continue;
				}
				publishResources.add(resource);
			}
		}
		if (publishResources.size() > 0) {
			publish: {
				CmsPublishManager publishManager = OpenCms.getPublishManager();
				CmsPublishList publishList;
				try {
					publishList = publishManager.getPublishList(cmsObject, publishResources, false, false);
				}
				catch (CmsException e) {
					String message = "Error retrieving CmsPublishList from OpenCms";
					warnings.append(message).append("\n");
					LOG.warn(message, e);
					hasWarnings = true;
					break publish;
				}
				I_CmsReport report = new CmsLogReport(Locale.ENGLISH, OpenCmsIntelliJConnector.class);
				try {
					publishManager.publishProject(cmsObject, report, publishList);
				}
				catch (CmsException e) {
					String message = "Error publishing the resources";
					warnings.append(message).append("\n");
					LOG.warn(message, e);
					hasWarnings = true;
				}
			}
		}
		if (!hasWarnings) {
			println("OK");
		}
		else {
			println(warnings.toString());
		}
	}

	private String[] getStringArrayFromJSON(String json) {
		JSONArray jsonArray;
		try {
			jsonArray = (JSONArray)jsonParser.parse(json);
		}
		catch (ParseException e) {
			LOG.error("Exception parsing JSON parameters, aborting\nJSON: " + json, e);
			return null;
		}
		catch (ClassCastException e) {
			LOG.error("JSON can be parsed but cast to JSONArray throws Exception, aborting\nJSON: " + json, e);
			return null;
		}

		String[] arr = new String[jsonArray.size()];
		Iterator it = jsonArray.iterator();
		for (int i = 0; it.hasNext(); i++) {
			arr[i] = (String)it.next();
		}
		return arr;
	}

	private void println(String str) {
		try {
			out.println(str);
		}
		catch (IOException e) {
			LOG.error("printing to out is not possible", e);
		}
	}
}
