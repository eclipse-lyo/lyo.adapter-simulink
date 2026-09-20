package edu.gatech.mbsec.adapter.subversion;

import java.net.URI;
import java.net.URISyntaxException;


import org.eclipse.lyo.oslc4j.core.annotation.OslcDescription;
import org.eclipse.lyo.oslc4j.core.annotation.OslcName;
import org.eclipse.lyo.oslc4j.core.annotation.OslcNamespace;
import org.eclipse.lyo.oslc4j.core.annotation.OslcOccurs;
import org.eclipse.lyo.oslc4j.core.annotation.OslcPropertyDefinition;
import org.eclipse.lyo.oslc4j.core.annotation.OslcResourceShape;
import org.eclipse.lyo.oslc4j.core.annotation.OslcTitle;
import org.eclipse.lyo.oslc4j.core.annotation.OslcValueType;
import org.eclipse.lyo.oslc4j.core.model.AbstractResource;
import org.eclipse.lyo.oslc4j.core.model.Occurs;
import org.eclipse.lyo.oslc4j.core.model.ValueType;

@OslcNamespace(SubversionConstants.SUBVERSION_NAMESPACE)
@OslcName("File")
@OslcResourceShape(title = "Subversion File Resource Shape", describes = SubversionConstants.TYPE_SUBVERSION_FILE)
public class SubversionFile extends AbstractResource{
	
	public SubversionFile() throws URISyntaxException {
		super();
	}
	public SubversionFile(URI about) throws URISyntaxException {
		super(about);
	}

	private String name;

	public void setName(String name) {
		this.name = name;
	}

	@OslcDescription("Description of File::name TBD")
	@OslcName("name")
	@OslcOccurs(Occurs.ZeroOrOne)
	@OslcPropertyDefinition(SubversionConstants.SUBVERSION_NAMESPACE + "File/name")
	@OslcTitle("name")
	@OslcValueType(ValueType.XMLLiteral)
	public String getName() {
		 return name;
	}
	
	private String path;

	public void setPath(String path) {
		this.path = path;
	}

	@OslcDescription("Description of File::path TBD")
	@OslcName("path")
	@OslcOccurs(Occurs.ZeroOrOne)
	@OslcPropertyDefinition(SubversionConstants.SUBVERSION_NAMESPACE + "File/path")
	@OslcTitle("path")
	@OslcValueType(ValueType.XMLLiteral)
	public String getPath() {
		 return path;
	}
	
	private String author;

	public void setAuthor(String author) {
		this.author = author;
	}

	@OslcDescription("Description of File::author TBD")
	@OslcName("author")
	@OslcOccurs(Occurs.ZeroOrOne)
	@OslcPropertyDefinition(SubversionConstants.SUBVERSION_NAMESPACE + "File/author")
	@OslcTitle("author")
	@OslcValueType(ValueType.XMLLiteral)
	public String getAuthor() {
		 return author;
	}
	
	private String committedDate;

	public void setCommittedDate(String committedDate) {
		this.committedDate = committedDate;
	}

	@OslcDescription("Description of File::committedDate TBD")
	@OslcName("committedDate")
	@OslcOccurs(Occurs.ZeroOrOne)
	@OslcPropertyDefinition(SubversionConstants.SUBVERSION_NAMESPACE + "File/committedDate")
	@OslcTitle("committedDate")
	@OslcValueType(ValueType.XMLLiteral)
	public String getCommittedDate() {
		 return committedDate;
	}
	
	
	private String repositoryRootURL;

	public void setRepositoryRootURL(String repositoryRootURL) {
		this.repositoryRootURL = repositoryRootURL;
	}

	@OslcDescription("Description of File::repositoryRootURL TBD")
	@OslcName("repositoryRootURL")
	@OslcOccurs(Occurs.ZeroOrOne)
	@OslcPropertyDefinition(SubversionConstants.SUBVERSION_NAMESPACE + "File/repositoryRootURL")
	@OslcTitle("repositoryRootURL")
	@OslcValueType(ValueType.XMLLiteral)
	public String getRepositoryRootURL() {
		 return repositoryRootURL;
	}
	
	private String revision;

	public void setRevision(String revision) {
		this.revision = revision;
	}

	@OslcDescription("Description of File::revision TBD")
	@OslcName("revision")
	@OslcOccurs(Occurs.ZeroOrOne)
	@OslcPropertyDefinition(SubversionConstants.SUBVERSION_NAMESPACE + "File/revision")
	@OslcTitle("revision")
	@OslcValueType(ValueType.XMLLiteral)
	public String getRevision() {
		 return revision;
	}
	
	private String svnURL;

	public void setSvnURL(String svnURL) {
		this.svnURL = svnURL;
	}

	@OslcDescription("Description of File::svnURL TBD")
	@OslcName("svnURL")
	@OslcOccurs(Occurs.ZeroOrOne)
	@OslcPropertyDefinition(SubversionConstants.SUBVERSION_NAMESPACE + "File/svnURL")
	@OslcTitle("svnURL")
	@OslcValueType(ValueType.XMLLiteral)
	public String getSvnURL() {
		 return svnURL;
	}
	
	
	
}
