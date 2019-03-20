/**
 * Copyright 2019 Adauto Martins <adauto.martin@ideotech.com.br>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package br.com.ideotech.drawout.springBoot;


import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.test.context.junit4.SpringRunner;

import br.com.ideotech.drawout.model.Sample;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
public class SampleControllerTest {

	@Value("${local.server.port}")
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
