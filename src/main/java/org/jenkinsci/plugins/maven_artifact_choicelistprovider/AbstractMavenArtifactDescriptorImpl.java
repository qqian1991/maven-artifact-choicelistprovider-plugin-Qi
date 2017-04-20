package org.jenkinsci.plugins.maven_artifact_choicelistprovider;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.QueryParameter;

import com.cloudbees.plugins.credentials.CredentialsParameterDefinition.DescriptorImpl;

import hudson.model.Descriptor;
import hudson.util.FormValidation;
import jp.ikedam.jenkins.plugins.extensible_choice_parameter.ChoiceListProvider;

/**
 * 
 * Base Class for a Descriptor.
 *
 * @author stephan.watermeyer, Diebold Nixdorf
 */
public abstract class AbstractMavenArtifactDescriptorImpl extends Descriptor<ChoiceListProvider> {

	public FormValidation doCheckArtifactId(@QueryParameter String artifactId) {
		if (StringUtils.isBlank(artifactId)) {
			return FormValidation.error("The artifactId cannot be empty");
		}

		return FormValidation.ok();
	}

	public FormValidation doCheckPackaging(@QueryParameter String packaging) {
		if (!StringUtils.isBlank(packaging) && packaging.startsWith(".")) {
			return FormValidation.error("packaging must not start with a .");
		}

		return FormValidation.ok();
	}

	public FormValidation doCheckClassifier(@QueryParameter String classifier) {
		if (StringUtils.isBlank(classifier)) {
			FormValidation.ok("OK, will not filter for any classifier");
		}
		return FormValidation.ok();
	}

	/**
	 * [qq]
	 * @param pService s
	 * @param groupId g
	 * @param artifactId a
	 * @param packaging p
	 * @param version v
	 * @param repos r
	 * @param classifier c
	 * @param reverseOrder r
	 * @return result
	 */
	public FormValidation performTest(final IVersionReader pService,
									  @QueryParameter String groupId,
									  @QueryParameter String artifactId,
									  @QueryParameter String packaging,
									  @QueryParameter String version,
									  @QueryParameter String repos,
									  @QueryParameter String classifier,
									  @QueryParameter boolean reverseOrder) {
		if (StringUtils.isEmpty(packaging) && !StringUtils.isEmpty(classifier)) {
			return FormValidation.error(
					"You have choosen an empty Packaging configuration but have configured a Classifier. " +
							"Please either define a Packaging value or remove the Classifier");
		}

		try {
			final Map<String, String> entriesFromURL
                    = wrapTestConnection(pService, groupId, artifactId, packaging, version, repos, classifier, reverseOrder);

			if (entriesFromURL.isEmpty()) {
				return FormValidation.ok("(Working, but no Entries found)");
			}

			return FormValidation.ok(StringUtils.join(entriesFromURL.values(), '\n'));
		} catch (Exception e) {
			return FormValidation.error("error reading versions from url:" + e.getMessage());
		}
	}

	/**
     * [qq]
	 * Own implementations of this {@link DescriptorImpl} might do this normally as a static inner class. The
	 * surrounding class then has to extend {@link AbstractMavenArtifactChoiceListProvider} and thus this wrapper method
	 * can forward to the implementation of readURL.
	 * 
	 * @param service
	 *            TBD
	 * @param groupId
	 *            TBD
	 * @param artifactId
	 *            TBD
	 * @param packaging
	 *            TBD
     * @param version
     *            TBD
     * @param repos
     *            TBD
	 * @param classifier
	 *            TBD
	 * @param reverseOrder
	 *            TBD
	 * @return the list of found items.
	 */
	protected abstract Map<String, String> wrapTestConnection(IVersionReader service,
                                                              String groupId,
                                                              String artifactId,
                                                              String packaging,
                                                              String version,
                                                              String repos,
                                                              String classifier,
                                                              boolean reverseOrder);

}
