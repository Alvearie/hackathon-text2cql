package org.alvearie.dream.cql.snomed;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.alvearie.dream.cql.snomed.beans.Parameter;
import org.alvearie.dream.cql.snomed.beans.Part;
import org.alvearie.dream.cql.snomed.beans.Response;

import com.fasterxml.jackson.databind.ObjectMapper;

@Path("/expandSnoMed")
public class SnoMedExpander {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public static final Set<String> expandSnoMed(@QueryParam("snoMedCode") String snoMedCode) {
		try {
			URL url = new URL("https://snowstorm-fhir.snomedtools.org/fhir/CodeSystem/$lookup?code=" + snoMedCode);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");

			int status = con.getResponseCode();
			if (status == 200) {
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer content = new StringBuffer();
				while ((inputLine = in.readLine()) != null) {
					content.append(inputLine);
				}
				in.close();

				Set<String> childCodes = new HashSet<>();
				ObjectMapper objectMapper = new ObjectMapper();
				Response response = objectMapper.readValue(content.toString(), Response.class);

				for (Parameter parameter : response.getParameter()) {
					if (parameter.getPart() != null) {
						String code = null;
						String value = null;
						for (Part part : parameter.getPart()) {
							if (part.getName().equals("code")) {
								code = part.getValueString();
							} else if (part.getName().equals("value")) {
								value = part.getValueCode();
							}
						}
						if (code != null && code.equals("child") && value != null) {
							childCodes.add(value);
						}
					}
				}
				return childCodes;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Collections.emptySet();
	}
}