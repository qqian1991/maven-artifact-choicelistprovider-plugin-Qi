package org.jenkinsci.plugins.maven_artifact_choicelistprovider.central;

import java.util.Map;

import org.jenkinsci.plugins.maven_artifact_choicelistprovider.AbstractMavenArtifactChoiceListProvider;
import org.jenkinsci.plugins.maven_artifact_choicelistprovider.AbstractMavenArtifactDescriptorImpl;
import org.jenkinsci.plugins.maven_artifact_choicelistprovider.IVersionReader;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import hudson.Extension;
import hudson.util.FormValidation;
import jp.ikedam.jenkins.plugins.extensible_choice_parameter.ChoiceListProvider;

/**
 * 
 * The implementation of the {@link ChoiceListProvider} for MavenCentral repository.
 *
 * @author stephan.watermeyer, Diebold Nixdorf
 */
public class MavenCentralChoiceListProvider extends AbstractMavenArtifactChoiceListProvider {

	private static final long serialVersionUID = -4215624253720954168L;

	@DataBoundConstructor
	public MavenCentralChoiceListProvider(String artifactId) {
		super(artifactId);
	}

	@Extension
	public static class MavenDescriptorImpl extends AbstractMavenArtifactDescriptorImpl {

		/**
		 * the display name shown in the dropdown to select a choice provider.
		 * 
		 * @return display name
		 * @see hudson.model.Descriptor#getDisplayName()
		 */
		@Override
		public String getDisplayName() {
			return "MavenCentral Artifact Choice Parameter";
		}

		/**
		 * [qq]
		 * @param url u
		 * @param groupId g
		 * @param artifactId a
		 * @param packaging p
		 * @param version v
		 * @param respo r
		 * @param classifier c
		 * @param reverseOrder r
		 * @return result
		 */
		public FormValidation doTest(@QueryParameter String url,
									 @QueryParameter String groupId,
									 @QueryParameter String artifactId,
									 @QueryParameter String packaging,
									 @QueryParameter String version,
									 @QueryParameter String respo,
									 @QueryParameter String classifier,
									 @QueryParameter boolean reverseOrder) {
			final IVersionReader service = new MavenCentralSearchService();
			return super.performTest(service, groupId, artifactId, packaging, version, respo, classifier, reverseOrder);
		}

		/**
		 * [qq]
		 * @param service s
		 * @param pGroupId g
		 * @param pArtifactId a
		 * @param pPackaging p
		 * @param pVersion v
		 * @param pRepos r
		 * @param pClassifier c
		 * @param pReverseOrder r
		 * @return result
		 */
		@Override
		protected Map<String, String> wrapTestConnection(IVersionReader service,
														 String pGroupId,
														 String pArtifactId,
														 String pPackaging,
														 String pVersion,
														 String pRepos,
														 String pClassifier,
														 boolean pReverseOrder) {
			return readURL(new MavenCentralSearchService(), pGroupId, pArtifactId, pPackaging, pVersion, pRepos, pClassifier, pReverseOrder);
		}

	}

	public IVersionReader createServiceInstance() {
		return new MavenCentralSearchService();
	}

}
