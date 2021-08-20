# hackathon-text2cql
Generate CQL from medical text snippets - created as part of an Alvearie hackathon
This repository contains artifacts created as part of the first Watson Health Project Alvearie Hackathon.  The event took place April 7-9, 2021.

#### Folders
- key-concept-extractor (a service to extract coded concepts from text fragments)
- cql-generator (a service to generate a cql query from fragment classification and concept extraction)
- learned-intent (a service to classify a text fragment into a medical category **currently missing from this repository-coming soon**
- notebooks (provide the overall flow calling off to the services as needed)
- Patients (a collection of artificial patients, should be persisted into a fhir server)

#### Getting Started

The code in the repository relies on the following technologies.

##### Quarkus

This project is a collection of microservices where each stage exposes an API that will allow us to quickly test them individually as well as put them together into a useful sequence (for example, a Jupyter notebook).  In order to implement these services, [Quarkus](https://quarkus.io) was chosen to accelerate our ability to write Java based solutions.  Quarkus is a "full-stack, Kubernetes-native Java framework" that is designed to be easy to use right from the start with little or no configuration.  Java code written in a Quarkus environment can quickly be built to either run in a local mode for testing of the API endpoints or can be pushed as a container to a repository where an automatically created Kubernetes configuration file can be deployed to utilize the new existing container in a cloud based deployment.

##### Jupyter

In order to glue all of these services together into a cohesive pipeline, we rely on [Project Jupyter](https://jupyter.org), in particular simple Jupyter notebooks.  A notebook is a series of cells, in our case containing snippets of Python code, that could be executed standalone or in sequence to process the stages of the pipeline.  These code cells were intermixed with cells containing standard markdown for documenting the pipeline.  The notebook not only provided a good development environment but also allowed for easy demonstration of partial or final products.
