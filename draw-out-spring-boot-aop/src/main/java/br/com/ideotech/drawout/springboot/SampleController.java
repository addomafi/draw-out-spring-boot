package br.com.ideotech.drawout.springboot;


import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import br.com.ideotech.drawout.model.Sample;

@RestController
@RequestMapping("/sample")
public class SampleController {

	@RequestMapping(method = RequestMethod.GET, value= "/**")
	@ResponseBody String sample() {
		Sample s = new Sample();
		s.setId(19);
		s.setText("Client");
		
		Response response = ClientBuilder.newClient()
				.target("http://localhost:8080")
				.path("sample")
				.request(MediaType.APPLICATION_JSON).post(Entity.json(s));
		
		Sample ret = response.readEntity(Sample.class);
		return ret.getText();
	}
	
	@RequestMapping(method = RequestMethod.GET, value= "/exception/**")
	@ResponseBody String sampleDelete() throws SampleException {
		throw new SampleException("Some error!");
	}
	
	@RequestMapping(method = RequestMethod.GET, value= "/runtimeException/**")
	@ResponseBody String samplePut() throws SampleException {
		throw new SampleRuntimeException("Some runtime error!");
	}
	
	@RequestMapping(method = RequestMethod.POST, value= "/**")
	@ResponseBody Sample sample(HttpServletRequest request, @RequestBody Sample sample) {
		Sample s = new Sample();
		s.setId(19 + sample.getId());
		s.setText("Text to test " + sample.getText());
		return s;
	}
}
