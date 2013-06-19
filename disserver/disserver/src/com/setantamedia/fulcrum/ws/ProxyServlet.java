package com.setantamedia.fulcrum.ws;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import com.setantamedia.fulcrum.common.Utilities;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

public class ProxyServlet  extends HttpServlet {

	private String baseUrl = "http://localhost:8081";
	private HttpContext localContext = null;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		localContext = new BasicHttpContext();
	}

	protected String[] getPathElements(HttpServletRequest request) {
		String[] result = null;
		String pathInfo = request.getPathInfo();
		if (pathInfo != null) {
			String decodedURL = null;
			try {
				decodedURL = URLDecoder.decode(pathInfo, "UTF-8");
				result = decodedURL.split("/");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		int status = HttpServletResponse.SC_NOT_FOUND;
		try {
			String[] pathElements = getPathElements(request);
			HttpClient client = new DefaultHttpClient();
			HttpGet getRequest = new HttpGet();
			String url = baseUrl;
			for (String pathElement:pathElements) {
				url += "/" + pathElement;
			}
			String queryString = request.getQueryString();
			if (queryString != null && ! "".equals(queryString)) {
				url += "? + queryString";
			}
			getRequest.setURI(new URI(url));
			HttpResponse getResponse = client.execute(getRequest, localContext);

			HttpEntity entity = getResponse.getEntity();
			if (entity != null) {
				InputStream inStream = entity.getContent();
				for (Header header: getResponse.getAllHeaders()) {
					response.setHeader(header.getName(), header.getValue());
				}
				response.getWriter().write(Utilities.convertStreamToString(inStream));
				inStream.close();

				// TODO - handle status - just return ok for now while testing
				// StatusLine statusLine = response.getStatusLine();
				response.setStatus(HttpServletResponse.SC_OK);
				response.getWriter().flush();
				response.getWriter().close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			response.setStatus(status);             
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		int status = HttpServletResponse.SC_NOT_FOUND;
		try {
			String[] pathElements = getPathElements(request);
			HttpClient client = new DefaultHttpClient();
			HttpPost postRequest = new HttpPost();
			String url = baseUrl;
			for (String pathElement:pathElements) {
				url += "/" + pathElement;
			}
			String queryString = request.getQueryString();
			if (queryString != null && ! "".equals(queryString)) {
				url += "? + queryString";
			}
			postRequest.setURI(new URI(url));
			HttpResponse getResponse = client.execute(postRequest, localContext);

			HttpEntity entity = getResponse.getEntity();
			if (entity != null) {
				InputStream inStream = entity.getContent();
				for (Header header: getResponse.getAllHeaders()) {
					response.setHeader(header.getName(), header.getValue());
				}
				response.getWriter().write(Utilities.convertStreamToString(inStream));
				inStream.close();

				// TODO - handle status - just return ok for now while testing
				// StatusLine statusLine = response.getStatusLine();
				response.setStatus(HttpServletResponse.SC_OK);
				response.getWriter().flush();
				response.getWriter().close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			response.setStatus(status);             
		}
	}

	@Override
	public String getServletInfo() {
		return "Fulcrum Proxy Servlet";
	}

}
