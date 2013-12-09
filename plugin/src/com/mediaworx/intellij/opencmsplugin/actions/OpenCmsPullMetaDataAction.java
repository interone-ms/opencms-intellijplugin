package com.mediaworx.intellij.opencmsplugin.actions;

import com.mediaworx.intellij.opencmsplugin.configuration.OpenCmsPluginConfigurationData;
import com.mediaworx.intellij.opencmsplugin.sync.OpenCmsSyncer;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("ComponentNotRegistered")
public abstract class OpenCmsPullMetaDataAction extends OpenCmsSyncAction {

	@Override
	protected void setSyncerOptions(@NotNull OpenCmsSyncer syncer) {
		syncer.setPullMetaDataOnly(true);
	}

	protected boolean isEnabled() {
		OpenCmsPluginConfigurationData config = plugin.getPluginConfiguration();
		return config.isPluginConnectorEnabled() && config.isPullMetadataEnabled();
	}

}
