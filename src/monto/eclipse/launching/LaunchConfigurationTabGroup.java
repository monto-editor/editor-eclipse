package monto.eclipse.launching;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;

public class LaunchConfigurationTabGroup extends AbstractLaunchConfigurationTabGroup {
  @Override
  public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
    MainClassLaunchConfigurationTab mainTab = new MainClassLaunchConfigurationTab();
    setTabs(new ILaunchConfigurationTab[] {mainTab});
  }
}
