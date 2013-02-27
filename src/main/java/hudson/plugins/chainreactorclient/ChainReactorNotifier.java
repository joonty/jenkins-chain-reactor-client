/*
 * The MIT License
 *
 * Copyright 2013 Jon Cairns <jon@joncairns.com>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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

    protected void notifyChainReactorServers(AbstractBuild build, BuildListener listener) {
        ArrayList<ChainReactorServer> servers = ((DescriptorImpl) getDescriptor()).getServers();

        if (servers.isEmpty()) {
			listener.getLogger().println("* No chain reactor servers have been set up - this can be done in the Jenkins global configuration *");
        } else {
			listener.getLogger().println("Notifying chain reaction server(s):");
            ChainReactorConnector conn = new ChainReactorConnector(build,listener.getLogger());
            for (ChainReactorServer s : servers) {
                listener.getLogger().println("\t- "+s.toString());
                conn.connect(s);
            }
        }
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

		if (crclientEnabled == false) {
			return true;
		} else {
            notifyChainReactorServers(build,listener);
		}
		return true;
	}

	@Extension
	public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

		public ArrayList<ChainReactorServer> crservers = new ArrayList<ChainReactorServer>();

		public DescriptorImpl() {
			super(ChainReactorNotifier.class);
			load();
		}

		public ArrayList<ChainReactorServer> getServers() {
			return crservers;
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
			crservers.clear();
			for (Object data : getArray(json.get("crservers"))) {
				ChainReactorServer s = req.bindJSON(ChainReactorServer.class, (JSONObject) data);
				crservers.add(s);
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
