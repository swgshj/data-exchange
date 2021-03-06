package com.ge.predix.solsvc.fdh.router.it;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.cxf.helpers.IOUtils;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mimosa.osacbmv3_3.OsacbmDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.ge.predix.entity.field.fieldidentifier.FieldSourceEnum;
import com.ge.predix.entity.getfielddata.GetFieldDataRequest;
import com.ge.predix.entity.getfielddata.GetFieldDataResult;
import com.ge.predix.entity.timeseries.datapoints.queryresponse.DatapointsResponse;
import com.ge.predix.solsvc.ext.util.JsonMapper;
import com.ge.predix.solsvc.fdh.handler.asset.AssetGetFieldDataHandlerImpl;
import com.ge.predix.solsvc.fdh.handler.timeseries.TimeseriesGetDataHandler;
import com.ge.predix.solsvc.fdh.router.boot.FdhRouterApplication;
import com.ge.predix.solsvc.fdh.router.service.router.GetRouter;
import com.ge.predix.solsvc.fdh.router.util.TestData;
import com.ge.predix.solsvc.restclient.config.IOauthRestConfig;
import com.ge.predix.solsvc.restclient.impl.RestClient;

/**
 * 
 * @author predix
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { FdhRouterApplication.class,
		AssetGetFieldDataHandlerImpl.class, TimeseriesGetDataHandler.class,
		GetRouter.class })
@WebAppConfiguration
@IntegrationTest({ "server.port=9092" })
public class TimeseriesGetFieldDataIT {

	private static final Logger log = LoggerFactory
			.getLogger(TimeseriesGetFieldDataIT.class);

	@Autowired
	private RestClient restClient;

	@Autowired
	private JsonMapper jsonMapper;

	@Autowired
	@Qualifier("defaultOauthRestConfig")
	private IOauthRestConfig restConfig;

	/**
	 * @throws Exception
	 *             -
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// new RestTemplate();
	}

	/**
	 * @throws Exception
	 *             -
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		//
	}

	/**
	 * @throws Exception
	 *             -
	 */
	@SuppressWarnings({})
	@Before
	public void setUp() throws Exception {
		this.jsonMapper.init();
	}

	/**
	 * @throws Exception
	 *             -
	 */
	@After
	public void tearDown() throws Exception {
		//
	}

	/**
	 * @throws IllegalStateException
	 *             -
	 * @throws IOException
	 *             -
	 */
	@SuppressWarnings("nls")
	@Test
	public void testGetFieldDataByTimeSeriesOnly()
			throws IllegalStateException, IOException {		
		testGetFieldData("GetFieldDataRequestWithTS.json");		
	}

	/**
	 * @throws IllegalStateException
	 *             -
	 * @throws IOException
	 *             -
	 */
	@SuppressWarnings("nls")
	@Test
	public void testGetFieldDataByAssetCriteriaAwareTimeSeries()
			throws IllegalStateException, IOException {		
		testGetFieldData("GetFieldDataRequestAssetAndTS.json");
	}

	private void testGetFieldData(String requestFile) throws IllegalStateException, IOException {

		log.debug("================================");
		String request = IOUtils.toString(getClass().getClassLoader()
				.getResourceAsStream(requestFile));

		String url = "http://localhost:" + "9092"
				+ "/services/fdhrouter/fielddatahandler/getfielddata";
		log.debug("URL = " + url);

		List<Header> headers = new ArrayList<Header>();
		headers.add(new BasicHeader("Content-Type", "application/json"));
		this.restClient.addSecureTokenForHeaders(headers);

		CloseableHttpResponse response = null;
		try {
			response = this.restClient.post(url, request, headers, 1000000,
					1000000);

			log.debug("RESPONSE: Response from Get Field Data  = " + response);

			String responseAsString = this.restClient.getResponse(response);
			log.debug("RESPONSE: Response from Get Field Data  = "
					+ responseAsString);

			Assert.assertNotNull(response);
			Assert.assertNotNull(responseAsString);
			Assert.assertFalse(responseAsString.contains("\"status\":500"));
			Assert.assertFalse(responseAsString
					.contains("Internal Server Error"));
			Assert.assertTrue(responseAsString.contains("errorEvent\":[]"));
			GetFieldDataResult getFieldDataResponse = this.jsonMapper.fromJson(
					responseAsString, GetFieldDataResult.class);
			Assert.assertTrue(getFieldDataResponse.getFieldData().get(0)
					.getData() instanceof DatapointsResponse);
			DatapointsResponse dpResponse = (DatapointsResponse) getFieldDataResponse
					.getFieldData().get(0).getData();
			Assert.assertTrue(dpResponse.getTags().size() > 0);
			Assert.assertTrue(dpResponse.getTags().get(0).getStats()
					.getRawCount() > 0);
		} finally {
			if (response != null)
				response.close();

		}

	}	
}
