package gr.iti.mklab.framework.common.domain;

import org.mongodb.morphia.annotations.Entity;


@Entity(noClassnameStored = true)
public class Concept extends JSONable {

	private static final long serialVersionUID = -7389160446822526436L;
	
	private ConceptType type;

	@Entity(noClassnameStored = true)
	public enum ConceptType {
		graphics,
		messages,
		keepcalm,
		memes,
		porn,
		disturbing,
		images,
		heavytext
	}
	
	public Concept(String type, Double score) {
		this.type = ConceptType.valueOf(type);
		this.score = score;
	}
	
	public Concept(ConceptType type, Double score) {
		this.type = type;
		this.score = score;
	}
	
	public String getConcept() {
		return type.toString();
	}
	
}
