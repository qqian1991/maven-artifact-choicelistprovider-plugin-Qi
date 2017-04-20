package org.jenkinsci.plugins.maven_artifact_choicelistprovider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.maven_artifact_choicelistprovider.artifactory.ArtifactoryChoiceListProvider;
import org.jenkinsci.plugins.maven_artifact_choicelistprovider.central.MavenCentralChoiceListProvider;
import org.jenkinsci.plugins.maven_artifact_choicelistprovider.nexus.NexusChoiceListProvider;
import org.kohsuke.stapler.DataBoundSetter;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;

import hudson.ExtensionPoint;
import hudson.security.ACL;
import jenkins.model.Jenkins;
import jp.ikedam.jenkins.plugins.extensible_choice_parameter.ChoiceListProvider;

/**
 * 
 * Base Class for different {@link ChoiceListProvider} that can display information from an artifact repository, like
 * {@link NexusChoiceListProvider}, {@link MavenCentralChoiceListProvider} and {@link ArtifactoryChoiceListProvider}
 *
 * @author stephan.watermeyer, Diebold Nixdorf
 */
public abstract class AbstractMavenArtifactChoiceListProvider extends ChoiceListProvider implements ExtensionPoint {

	private static final long serialVersionUID = -6055763342458172275L;

	private static final Logger LOGGER = Logger.getLogger(AbstractMavenArtifactChoiceListProvider.class.getName());

	private String groupId;
	private String artifactId;
	private String packaging;
    private String version;
    private String repos;
	private String classifier;
	private boolean reverseOrder;

	/**
	 * Initializes the choiceliste with at the artifactId.
	 * 
	 * @param artifactId
	 *            the artifactId is the minimum required information.
	 */
	public AbstractMavenArtifactChoiceListProvider(final String artifactId) {
		super();
		this.setArtifactId(artifactId);
	}

	@Override
	public List<String> getChoiceList() {

		LOGGER.log(Level.FINE, "retrieve the versions from the repository");
		final Map<String, String> mChoices
                = readURL(createServiceInstance(), getGroupId(), getArtifactId(), getPackaging(), getVersion(), getRepos(), getClassifier(), getReverseOrder());
		// FIXME: CHANGE-1: Return only the keys, that are shorter then the values
		// return new ArrayList<String>(mChoices.keySet());
		return new ArrayList<String>(mChoices.values());
	}

	/**
	 * Different implementation will return different {@link IVersionReader} instances.
	 * 
	 * @return the source of the artifacts.
	 */
	public abstract IVersionReader createServiceInstance();

	/**
	 * Returns the {@link UsernamePasswordCredentialsImpl} for the given CredentialId
	 * 
	 * @param pCredentialId
	 *            the internal jenkins id for the credentials
	 * @return the credentials for the ID or NULL
	 */
	public static UsernamePasswordCredentialsImpl getCredentials(final String pCredentialId) {
		return CredentialsMatchers.firstOrNull(
				CredentialsProvider.lookupCredentials(UsernamePasswordCredentialsImpl.class, Jenkins.getInstance(),
						ACL.SYSTEM, Collections.<DomainRequirement> emptyList()),
				CredentialsMatchers.allOf(CredentialsMatchers.withId(pCredentialId)));
	}

	/**
	 * Retrieves the versions from the given source.
	 * 
	 * @param pInstance
	 *            the artifact repository service.
	 * @param pGroupId
	 *            the groupId of the artifact
	 * @param pArtifactId
	 *            the artifactId
     * @param pVersion version
     *
     * @param  pRepos repos
	 * @param pPackaging
	 *            the packaginging
	 * @param pClassifier
	 *            the classifier
	 * @param pReverseOrder
	 *            <code>true</code> if the result should be reversed.
	 * @return never null
	 */
	public static Map<String, String> readURL(final IVersionReader pInstance,
											  final String pGroupId,
											  final String pArtifactId,
											  final String pPackaging,
											  final String pVersion,
											  final String pRepos,
											  String pClassifier,
											  final boolean pReverseOrder) {
		Map<String, String> retVal = new LinkedHashMap<String, String>();
		try {
			ValidAndInvalidClassifier classifierBox = ValidAndInvalidClassifier.fromString(pClassifier);

			List<String> choices = pInstance.retrieveVersions(pGroupId, pArtifactId, pPackaging, pVersion, pRepos, classifierBox);

			if (pReverseOrder) {
                Collections.reverse(choices);
            }

			retVal = toMap(choices);
		} catch (VersionReaderException e) {
			LOGGER.log(Level.INFO, "failed to retrieve versions from repository for g:" + pGroupId + ", a:"
					+ pArtifactId + ", p:" + pPackaging + ", v:"+ pVersion + ", repos:" + pRepos+ ", c:" + pClassifier, e);
			retVal.put("error", e.getMessage());
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "failed to retrieve versions from nexus for g:" + pGroupId + ", a:" + pArtifactId
					+ ", p:" + pPackaging + ", v:"+ pVersion + ", repos:" + pRepos+ ", c:" + pClassifier, e);
			retVal.put("error", "Unexpected Error: " + e.getMessage());
		}
		return retVal;
	}

	/**
	 * Cuts of the first parts of the URL and only returns a smaller set of items.
	 * 
	 * @param pChoices
	 *            the list which is transformed to a map
	 * @return the map containing the short url as Key and the long url as value.
	 */
	public static Map<String, String> toMap(List<String> pChoices) {
		Map<String, String> retVal = new LinkedHashMap<String, String>();
		for (String current : pChoices) {
			retVal.put(current.substring(current.lastIndexOf("/") + 1), current);
		}
		return retVal;
	}

	@DataBoundSetter
	public void setGroupId(String groupId) {
		this.groupId = StringUtils.trim(groupId);
	}

	@DataBoundSetter
	public void setArtifactId(String artifactId) {
		this.artifactId = StringUtils.trim(artifactId);
	}

	@DataBoundSetter
	public void setPackaging(String packaging) {
		this.packaging = StringUtils.trim(packaging);
	}

	@DataBoundSetter
	public void setVersion(String version){
        this.version = StringUtils.trim(version);
    }
	
	@DataBoundSetter
    public void setRepos(String repos){
        this.repos = StringUtils.trim(repos);
    }

	@DataBoundSetter
	public void setClassifier(String classifier) {
		this.classifier = StringUtils.trim(classifier);
	}

	@DataBoundSetter
	public void setReverseOrder(boolean reverseOrder) {
		this.reverseOrder = reverseOrder;
	}

	public String getGroupId() {
		return groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public String getPackaging() {
		return packaging;
	}

	public String getVersion(){
        return this.version;
    }

    public String getRepos(){
        return this.repos;
    }

	public String getClassifier() {
		return classifier;
	}

	public boolean getReverseOrder() {
		return reverseOrder;
	}

}
