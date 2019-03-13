package br.com.ideotech.drawout.springBoot;


import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.test.context.junit4.SpringRunner;

import br.com.ideotech.drawout.model.Sample;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
public class SampleControllerTest {
	
	@LocalServerPort
    private int port;
	
	@Autowired
    private TestRestTemplate restTemplate;
	
	@Test
    public void testSample() {
		assertThat("Response is equal...",
				this.restTemplate.getForObject("http://localhost:" + port + "/sample?a=01&b=02", String.class), 
				equalTo("Text to test Client"));
		
		assertThat("Response is equal...",
				this.restTemplate.getForObject("http://localhost:" + port + "/", String.class), 
				containsString("404"));
		
		assertThat("Response is equal...",
				this.restTemplate.getForObject("http://localhost:" + port + "/sample/exception", String.class), 
				containsString("Some error!"));
		
		assertThat("Response is equal...",
				this.restTemplate.getForObject("http://localhost:" + port + "/sample/runtimeException", String.class), 
				containsString("Some runtime error!"));
		
		HttpEntity<Sample> req = new HttpEntity<Sample>(new Sample());
		req.getBody().setId(1);
		req.getBody().setText("Text to test");
		
		Sample ret = new Sample();
		ret.setId(20);
		ret.setText("Text to test Text to test");
		assertThat("Response is equal...",
				this.restTemplate.postForObject("http://localhost:" + port + "/sample", req, Sample.class), 
				equalTo(ret));
		
    }
}
