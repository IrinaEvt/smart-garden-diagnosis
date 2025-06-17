package com.example.smart_garden.ontology;

import com.example.smart_garden.models.Plant;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.OWLEntityRemover;

import java.io.File;
import java.util.*;

public class PlantOntology {

    private OWLOntologyManager ontoManager;
    private OWLOntology plantOntology;
    private OWLDataFactory dataFactory;
    private String ontologyIRIStr;
    private OWLReasoner reasoner;

    public PlantOntology() {
        ontoManager = OWLManager.createOWLOntologyManager();
        dataFactory = ontoManager.getOWLDataFactory();
        loadOntology();
        ontologyIRIStr = plantOntology.getOntologyID().getOntologyIRI().get().toString() + "#";
        OWLReasonerFactory reasonerFactory = new ReasonerFactory();
        reasoner = reasonerFactory.createReasoner(plantOntology);
    }

    private void loadOntology() {
        File ontology = new File("src/main/resources/plant_ontology.owl");
        try {
            plantOntology = ontoManager.loadOntologyFromOntologyDocument(ontology);
        } catch (OWLOntologyCreationException e) {
            System.out.println(e.getMessage());
        }
    }

    private String getClassFriendlyName(OWLClassExpression expr) {
        if (!expr.isAnonymous()) {
            String iri = expr.asOWLClass().getIRI().toString();
            return iri.substring(iri.indexOf("#") + 1);
        }
        return expr.toString();
    }

    private String getIndividualFriendlyName(IRI iri) {
        String iriStr = iri.toString();
        return iriStr.substring(iriStr.indexOf("#") + 1);
    }

    public void createPlantIndividual(Plant plant) {
        OWLNamedIndividual plantIndiv = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + plant.getName()));
        OWLClass plantType = dataFactory.getOWLClass(IRI.create(ontologyIRIStr + plant.getType()));
        if (!plantOntology.containsClassInSignature(plantType.getIRI())) {
            OWLDeclarationAxiom declareClass = dataFactory.getOWLDeclarationAxiom(plantType);
            ontoManager.applyChange(new AddAxiom(plantOntology, declareClass));
        }
        OWLClassAssertionAxiom axPlant = dataFactory.getOWLClassAssertionAxiom(plantType, plantIndiv);
        ontoManager.applyChange(new AddAxiom(plantOntology, axPlant));
    }

    public void createSymptomForPlant(String plantName, String symptomName) {
        OWLNamedIndividual plantIndiv = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + plantName));
        String symptomIndivName = plantName + "_" + symptomName;
        OWLNamedIndividual symptomIndiv = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + symptomIndivName));
        OWLClass symptomClass = dataFactory.getOWLClass(IRI.create(ontologyIRIStr + symptomName));

        OWLClassAssertionAxiom classAssertion = dataFactory.getOWLClassAssertionAxiom(symptomClass, symptomIndiv);
        ontoManager.applyChange(new AddAxiom(plantOntology, classAssertion));

        OWLObjectProperty hasSymptom = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "hasSymptom"));
        OWLObjectPropertyAssertionAxiom link = dataFactory.getOWLObjectPropertyAssertionAxiom(hasSymptom, plantIndiv, symptomIndiv);
        ontoManager.applyChange(new AddAxiom(plantOntology, link));
        reasoner.flush();
    }

  /*  public List<String> getAdviceForPlantIndividual(String plantIndivName) {
        List<String> result = new ArrayList<>();
        OWLNamedIndividual plantIndiv = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + plantIndivName));
        OWLObjectProperty hasSymptom = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "hasSymptom"));

        for (OWLObjectPropertyAssertionAxiom ax : plantOntology.getObjectPropertyAssertionAxioms(plantIndiv)) {
            if (ax.getProperty().asOWLObjectProperty().equals(hasSymptom)) {
                OWLNamedIndividual symptomIndiv = ax.getObject().asOWLNamedIndividual();
                result.add("Симптом: " + getIndividualFriendlyName(symptomIndiv.getIRI()));

                Set<OWLClass> symptomTypes = reasoner.getTypes(symptomIndiv, true).getFlattened();
                for (OWLClass symptomClass : symptomTypes) {
                    for (OWLSubClassOfAxiom sa : plantOntology.getSubClassAxiomsForSubClass(symptomClass)) {
                        if (sa.getSuperClass() instanceof OWLObjectSomeValuesFrom) {
                            OWLObjectSomeValuesFrom restriction = (OWLObjectSomeValuesFrom) sa.getSuperClass();
                            OWLObjectPropertyExpression prop = restriction.getProperty();
                            OWLClassExpression filler = restriction.getFiller();

                            if (!filler.isAnonymous()) {
                                String propName = getClassFriendlyName(dataFactory.getOWLClass(prop.getNamedProperty().getIRI()));
                                String causeName = getClassFriendlyName(filler.asOWLClass());

                                if (propName.toLowerCase().contains("cause")) {
                                    result.add("Възможна причина: " + causeName);

                                    // Разширение: търси care actions за тази причина
                                    OWLClass causeClass = filler.asOWLClass();
                                    for (OWLSubClassOfAxiom ca : plantOntology.getSubClassAxiomsForSubClass(causeClass)) {
                                        if (ca.getSuperClass() instanceof OWLObjectSomeValuesFrom) {
                                            OWLObjectSomeValuesFrom actionRestriction = (OWLObjectSomeValuesFrom) ca.getSuperClass();
                                            OWLObjectProperty actionProp = actionRestriction.getProperty().asOWLObjectProperty();
                                            OWLClassExpression actionExpr = actionRestriction.getFiller();

                                            if (!actionExpr.isAnonymous()) {
                                                OWLClass actionClass = actionExpr.asOWLClass();
                                                String actionPropName = getClassFriendlyName(dataFactory.getOWLClass(actionProp.getIRI()));
                                                String actionClassName = getClassFriendlyName(actionClass);

                                                if (actionPropName.toLowerCase().contains("care") || actionPropName.toLowerCase().contains("treat")) {
                                                    // Потърси индивидуал от този клас (ако има)
                                                    boolean found = false;
                                                    for (OWLNamedIndividual indiv : plantOntology.getIndividualsInSignature()) {
                                                        for (OWLClassExpression t : reasoner.getTypes(indiv, true).getFlattened()) {
                                                            if (!t.isAnonymous() && t.asOWLClass().equals(actionClass)) {
                                                                result.add("Препоръчано действие: " + getIndividualFriendlyName(indiv.getIRI()));
                                                                found = true;
                                                            }
                                                        }
                                                    }
                                                    if (!found) {
                                                        // Върни името на класа като fallback
                                                        result.add("Препоръчано действие: " + actionClassName);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

   */

    public List<String> getAdviceForPlantIndividual(String plantIndivName) {
        List<String> result = new ArrayList<>();
        OWLNamedIndividual plantIndiv = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + plantIndivName));
        OWLObjectProperty hasSymptom = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "hasSymptom"));

        for (OWLObjectPropertyAssertionAxiom ax : plantOntology.getObjectPropertyAssertionAxioms(plantIndiv)) {
            if (ax.getProperty().equals(hasSymptom)) {
                OWLNamedIndividual symptomIndiv = ax.getObject().asOWLNamedIndividual();
                String symptomName = getIndividualFriendlyName(symptomIndiv.getIRI());
                result.add("Симптом: " + symptomName);

                // Reason за типа на симптома
                Set<OWLClass> symptomTypes = reasoner.getTypes(symptomIndiv, true).getFlattened();
                for (OWLClass symptomClass : symptomTypes) {
                    for (OWLSubClassOfAxiom sa : plantOntology.getSubClassAxiomsForSubClass(symptomClass)) {
                        if (sa.getSuperClass() instanceof OWLObjectSomeValuesFrom) {
                            OWLObjectSomeValuesFrom restriction = (OWLObjectSomeValuesFrom) sa.getSuperClass();
                            OWLObjectPropertyExpression prop = restriction.getProperty();
                            OWLClassExpression filler = restriction.getFiller();

                            if (!filler.isAnonymous()) {
                                OWLClass causeClass = filler.asOWLClass();
                                String causeName = getClassFriendlyName(causeClass);

                                String propName = getClassFriendlyName(
                                        dataFactory.getOWLClass(prop.asOWLObjectProperty().getIRI())
                                );

                                if (propName.toLowerCase().contains("cause")) {
                                    result.add("Възможна причина: " + causeName);

                                    // Потърси care action за причината
                                    for (OWLSubClassOfAxiom ca : plantOntology.getSubClassAxiomsForSubClass(causeClass)) {
                                        if (ca.getSuperClass() instanceof OWLObjectSomeValuesFrom) {
                                            OWLObjectSomeValuesFrom actionRestriction = (OWLObjectSomeValuesFrom) ca.getSuperClass();
                                            OWLObjectProperty actionProp = actionRestriction.getProperty().asOWLObjectProperty();
                                            OWLClassExpression actionExpr = actionRestriction.getFiller();

                                            if (!actionExpr.isAnonymous()) {
                                                OWLClass actionClass = actionExpr.asOWLClass();
                                                String actionClassName = getClassFriendlyName(actionClass);

                                                boolean foundActionIndividual = false;

                                                for (OWLNamedIndividual actionIndiv : plantOntology.getIndividualsInSignature()) {
                                                    Set<OWLClass> actionTypes = reasoner.getTypes(actionIndiv, true).getFlattened();
                                                    for (OWLClass type : actionTypes) {
                                                        if (type.equals(actionClass)) {
                                                            String actionName = getIndividualFriendlyName(actionIndiv.getIRI());
                                                            String actionPropName = getClassFriendlyName(
                                                                    dataFactory.getOWLClass(actionProp.getIRI())
                                                            );

                                                            if (actionPropName.toLowerCase().contains("care")
                                                                    || actionPropName.toLowerCase().contains("treat")) {
                                                                result.add("Препоръчано действие: " + actionName);
                                                                foundActionIndividual = true;
                                                            }
                                                        }
                                                    }
                                                }

                                                if (!foundActionIndividual) {
                                                    result.add("Препоръчано действие (клас): " + actionClassName);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return result;
    }



    public void removePlantAndSymptoms(String plantName, List<String> symptomNames) {
        OWLEntityRemover remover = new OWLEntityRemover(plantOntology.getImportsClosure());

        OWLNamedIndividual plantIndiv = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + plantName));
        plantIndiv.accept(remover);

        if (symptomNames != null) {
            for (String symptom : symptomNames) {
                String symptomIndivName = plantName + "_" + symptom;
                OWLNamedIndividual symptomIndiv = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + symptomIndivName));
                symptomIndiv.accept(remover);
            }
        }

        ontoManager.applyChanges(remover.getChanges());
    }

    public Map<String, String> getNeedsFromPlantType(String typeName) {
        Map<String, String> result = new LinkedHashMap<>();

        OWLClass plantClass = dataFactory.getOWLClass(IRI.create(ontologyIRIStr + typeName));
        OWLObjectProperty hasNeed = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "hasNeed"));

        for (OWLSubClassOfAxiom ax : plantOntology.getSubClassAxiomsForSubClass(plantClass)) {
            if (ax.getSuperClass() instanceof OWLObjectExactCardinality) {
                OWLObjectExactCardinality restriction = (OWLObjectExactCardinality) ax.getSuperClass();
                if (restriction.getProperty().asOWLObjectProperty().equals(hasNeed)) {
                    OWLClassExpression filler = restriction.getFiller();

                    if (!filler.isAnonymous()) {
                        String needName = getClassFriendlyName(filler.asOWLClass());

                        if (needName.contains("Water")) result.put("soilMoisture", needName.contains("Frequent") ? "high" : "low");
                        if (needName.contains("Humidity")) result.put("humidity", needName.contains("High") ? "high" : "low");
                        if (needName.contains("Light")) result.put("light", needName.contains("High") ? "high" : "low");
                        if (needName.contains("Temperature")) result.put("temperature", needName.contains("High") ? "high" : "low");
                    }
                }
            }
        }

        return result;
    }

    public List<String> getNeedsForPlant(String plantIndivName) {
        List<String> result = new ArrayList<>();
        OWLNamedIndividual plant = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + plantIndivName));
        OWLObjectProperty hasNeed = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "hasNeed"));

        for (OWLObjectPropertyAssertionAxiom ax : plantOntology.getObjectPropertyAssertionAxioms(plant)) {
            if (ax.getProperty().asOWLObjectProperty().equals(hasNeed)) {
                OWLNamedIndividual needIndiv = ax.getObject().asOWLNamedIndividual();
                StringBuilder sb = new StringBuilder();
                sb.append("Нужда: ").append(getIndividualFriendlyName(needIndiv.getIRI()));

                Set<OWLClass> types = reasoner.getTypes(needIndiv, true).getFlattened();
                for (OWLClass type : types) {
                    sb.append(" от тип ").append(getClassFriendlyName(type));
                }

                for (OWLDataPropertyAssertionAxiom dp : plantOntology.getDataPropertyAssertionAxioms(needIndiv)) {
                    OWLDataProperty property = dp.getProperty().asOWLDataProperty();
                    String propName = getIndividualFriendlyName(property.getIRI());
                    sb.append(" | ").append(propName).append(" = ").append(dp.getObject());
                }

                result.add(sb.toString());
            }
        }
        return result;
    }

    public Plant getPlantByIndividualName(String plantIndivName) {
        Plant plantModel = new Plant();
        plantModel.setName(plantIndivName);

        List<String> symptoms = new ArrayList<>();
        OWLNamedIndividual plantIndiv = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + plantIndivName));

        Set<OWLClass> types = reasoner.getTypes(plantIndiv, true).getFlattened();
        for (OWLClass plantClass : types) {
            plantModel.setType(getClassFriendlyName(plantClass));
        }

        OWLObjectProperty hasSymptom = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "hasSymptom"));
        for (OWLObjectPropertyAssertionAxiom ax : plantOntology.getObjectPropertyAssertionAxioms(plantIndiv)) {
            if (ax.getProperty().equals(hasSymptom)) {
                OWLNamedIndividual symptom = ax.getObject().asOWLNamedIndividual();
                symptoms.add(getIndividualFriendlyName(symptom.getIRI()));
            }
        }
        plantModel.setSymptoms(symptoms);
        return plantModel;
    }

    public void saveOntology() {
        try {
            ontoManager.saveOntology(plantOntology);
        } catch (OWLOntologyStorageException e) {
            System.out.println("Error saving ontology: " + e.getMessage());
        }
    }
}
