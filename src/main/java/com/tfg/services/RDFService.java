package com.tfg.services;

import com.tfg.models.Csv;
import com.tfg.models.Triplets;
import com.tfg.models.security.User;
import com.tfg.repositories.TripletsRepository;
import com.tfg.repositories.UserRepository;
import com.tfg.utils.CsvReader;

import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class RDFService {

    @Autowired
    private TripletsRepository tripletsRepository;

    @Autowired
    private UserRepository userRepository;

    private final String NS = "http://provisionalUri.com/";

    public List<Model> createRDF(File file) throws IOException {
        Csv csv = CsvReader.convertFileToCsv(file);
        List<Model> modelList = new ArrayList<>();
        for(int i = 1; i < csv.lines.length; i++) {
            Model model = ModelFactory.createDefaultModel();
            Resource r = model.createResource( NS + i);
            addProperties(r, csv, model, i);
            modelList.add(model);
        }


        return modelList;
    }

    private void addProperties(Resource r, Csv csv, Model model, int i) {
        for(int j = 0; j < csv.lines[i].length; j++) { // if the columns have different length this will cause problems
            Property property = model.createProperty(NS + csv.headers[j]);
            Literal value = model.createLiteral(csv.lines[i][j]);
            model.add(r, property, value);
        }

    }

    public char[] modelToChar(List<Model> models) {
        char[] rdf;
        StringBuilder all_elements = new StringBuilder();
        for(Model model: models) {
            StringWriter out = new StringWriter();
            model.write(out, "RDF/XML-ABBREV");
            all_elements.append(out.toString());
            all_elements.append("\n");
        }
        rdf = all_elements.toString().toCharArray();
        return rdf;
    }

    public String modelToString(List<Model> models) {
        StringBuilder all_elements = new StringBuilder();
        for(Model model: models) {
            StringWriter out = new StringWriter();
            model.write(out, "RDF/XML-ABBREV");
            RDFDataMgr.write(System.out, model, Lang.NTRIPLES);
            RDFDataMgr.write(System.out, model, Lang.RDFXML);
            all_elements.append(out.toString());
            all_elements.append("\n");
        }

        return all_elements.toString();
    }

    public void saveToDatabase(List<Model> rdf, String username) {
        if(!userRepository.findByUsername(username).isEmpty()) {
            Triplets triplets = new Triplets();
            triplets.setUser(username);
            triplets.setRdf(modelToChar(rdf));
            tripletsRepository.save(triplets);
            return;
        }
        throw new UsernameNotFoundException(username);
    }


/*
    public List<Model> createRDF(File file) throws Exception {
        Csv csv = CsvReader.convertFileToCsv(file);

        Property[] headersProperties = new Property[csv.headers.length];
        // initializing resources
        initializeProperties(headersProperties, csv);

        List<Model> models = initializeModels(csv, headersProperties);
        return models;
    }


    private List<Model> initializeModels(Csv csv, Property[] headersProperties){
        Model[] models = new Model[headersProperties.length];
        for (int i = 0; i < headersProperties.length; i++){
            models[i] = ModelFactory.createDefaultModel();
            Resource resource = models[i].createResource("https://provisional.uri/" + );
            for(int j = 1; j < csv.lines[i].length; j++) { //starts at 1 because 0 should be the subject info
                resource.addProperty(headersProperties[j], csv.lines[i][j]);
            }
        }
        return Arrays.asList(models);
    }

    private void initializeProperties(Resource[] headersProperties, Csv csv) {
        for (int i = 0; i < csv.headers.length; i++) {
            headersProperties[i] = getPredicate(csv.headers[i].toLowerCase());
        }
    }

    private Property getPredicate(String header) {
        return PropertiesFactory.getProperty(header);
    }
    */


}
