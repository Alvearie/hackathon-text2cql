package org.alvearie.dream.cql;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.alvearie.dream.concept.ConceptSet;
import org.alvearie.dream.cql.processors.AgeGenderTypeProcessor;
import org.alvearie.dream.cql.processors.ConditionTypeProcessor;
import org.alvearie.dream.cql.processors.DiabetesTypeProcessor;
import org.alvearie.dream.cql.processors.LabTypeProcessor;
import org.alvearie.dream.cql.processors.NoPregnancyNursingTypeProcessor;
import org.alvearie.dream.cql.processors.RMETypeProcessor;

import com.fasterxml.jackson.databind.ObjectMapper;

//run ./mvnw package
//
//run ./mvnw compile quarkus:dev  Service is up and running

@Path("/")
public class CQLGenerator {

	private static final ObjectMapper mapper = new ObjectMapper();

	private static final Set<TypeProcessor> typeProcessors = new HashSet<>(Arrays.asList(
			new AgeGenderTypeProcessor(),
			new RMETypeProcessor(), 
			new NoPregnancyNursingTypeProcessor(), 
			new DiabetesTypeProcessor(), 
			new ConditionTypeProcessor(),
			new LabTypeProcessor()));

	@GET
	@Path("/health")
	@Produces(MediaType.TEXT_PLAIN)
	public String generateCQL() {
		return "Service is up and running";
	}

	@POST
	@Path("/generate")
	@Produces(MediaType.TEXT_PLAIN)
	public String generate(@FormParam("conceptSet") String conceptSetJSON) {
		try {
			ConceptSet conceptSet = mapper.readValue(conceptSetJSON, ConceptSet.class);

			for (TypeProcessor typeProcessor : typeProcessors) {
				if (typeProcessor.getSupportedTypes().stream().anyMatch(conceptSet.getType()::equalsIgnoreCase)) {
					return typeProcessor.process(conceptSet.getConcepts());
				}
			}
			return TypeProcessor.NO_CQL_GENERATED + "\n\nLearned intent not supported";
		} catch (Exception e) {
			e.printStackTrace();
			return "error occurred" + e.getMessage();
		}
	}

	public static void main(String[] args) {
		String jsoninput = "{\"concepts\":[{\"code\":\"C0001779\",\"codeSystem\":\"UMLS\",\"trigger\":\"less than\",\"units\":\"years\",\"value\":\"25\"},{\"code\":\"73211009\",\"codeSystem\":\"SNOMED\"},{\"code\":\"446141000124107\",\"codeSystem\":\"SNOMED\"}],\"negated\":false,\"type\":\"ageGender\"}";

		CQLGenerator c = new CQLGenerator();
		String result = c.generate(jsoninput);
		System.out.println(result);
	}
}