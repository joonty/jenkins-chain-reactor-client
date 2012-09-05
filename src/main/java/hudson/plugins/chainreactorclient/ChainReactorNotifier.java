package hudson.plugins.chainreactorclient;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.IOException;

import java.io.PrintStream;
import org.kohsuke.stapler.StaplerRequest;
import net.sf.json.JSONObject;
import net.sf.json.JSONArray;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author Jon Cairns
 *
 */
public class ChainReactorNotifier extends Notifier {

	private static final Logger LOGGER = Logger.getLogger(ChainReactorNotifier.class.getName());
	public boolean crclientEnabled;

	@DataBoundConstructor
	public ChainReactorNotifier(boolean crclientEnabled) {
		this.crclientEnabled = crclientEnabled;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see hudson.tasks.BuildStep#getRequiredMonitorService()
	 */
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * hudson.tasks.BuildStepCompatibilityLayer#perform(hudson.model.AbstractBuild
	 * , hudson.Launcher, hudson.model.BuildListener)
	 */
	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
		BuildListener listener) throws InterruptedException, IOException {

		listener.getLogger().println("Chain reaction!!!");
		if (crclientEnabled == false) {
			return true;
		} else {
			listener.getLogger().println("Pineapples");
		}
		return true;
	}

	@Extension
	public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

		public ArrayList<ChainReactorServer> servers = new ArrayList<ChainReactorServer>();

		public DescriptorImpl() {
			super(ChainReactorNotifier.class);
			load();
		}

		public ArrayList<ChainReactorServer> getServers() {
			return servers;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see
		 * hudson.tasks.BuildStepDescriptor#isApplicable(java.lang.Class)
		 */
		@Override
		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return true;
		}

		@Override
		public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
			servers.clear();
			for (Object data : getArray(json.get("crservers"))) {
				ChainReactorServer s = req.bindJSON(ChainReactorServer.class, (JSONObject) data);
				servers.add(s);
			}
			save();
			return super.configure(req, json);
		}

		public static JSONArray getArray(Object data) {
			JSONArray result;
			if (data instanceof JSONArray) {
				result = (JSONArray) data;
			} else {
				result = new JSONArray();
				if (data != null) {
					result.add(data);
				}
			}
			return result;
		}
		/*
		 * (non-Javadoc)
		 *
		 * @see hudson.model.Descriptor#getDisplayName()
		 */
		@Override
		public String getDisplayName() {
			return "Chain Reactor Client";
		}
	}
}
