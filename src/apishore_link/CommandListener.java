package apishore_link;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

public class CommandListener extends Thread
{

	private static void toFront()
	{
		final Shell workbenchShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

		// bring up the application to front
		workbenchShell.setVisible(true);
		workbenchShell.setMinimized(false);
		workbenchShell.redraw();

		// focus on dialog
		workbenchShell.setActive();
		workbenchShell.forceActive();
		workbenchShell.setFocus();
		workbenchShell.forceFocus();
		workbenchShell.moveAbove(null);
		workbenchShell.redraw();
	}

	private String content;

	public CommandListener(final Activator activator)
	{
		this.start();
	}

	private void check(final File file) throws Exception
	{
		if(file.exists())
		{
			final String newContent = new String(Files.readAllBytes(file.toPath())).trim();
			if(!newContent.equals(this.content))
			{
				this.content = newContent;
				if(!this.content.isEmpty())
				{
					final int projectStart = this.content.indexOf("//") + 2;
					final int projectEnd = this.content.indexOf("/", projectStart);
					final String project = this.content.substring(projectStart, projectEnd);

					final int lineStart = this.content.indexOf(":", projectEnd);
					if(lineStart > 0)
					{
						final String fileName = this.content.substring(projectEnd, lineStart);
						final int columnStart = this.content.indexOf(":", lineStart + 1);
						if(columnStart > 0)
						{
							final int line = Integer.parseInt(this.content.substring(lineStart + 1, columnStart));
							final int col = Integer.parseInt(this.content.substring(columnStart + 1));
							openEditor(project, fileName, line, col);
						}
						else
						{
							final int line = Integer.parseInt(this.content.substring(lineStart + 1));
							openEditor(project, fileName, line, 1);
						}
					}
					else
					{
						final String fileName = this.content.substring(projectEnd);
						openEditor(project, fileName, 1, 1);
					}
				}
			}
		}
	}

	private void openEditor(final String projectName, final String fileName, final int line, final int col)
			throws CoreException
	{
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
					final IFile file = project.getFile(fileName);
					final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

					final HashMap<String, Object> map = new HashMap<>();
					map.put(IMarker.LINE_NUMBER, new Integer(line));
					final IMarker marker = file.createMarker(IMarker.TEXT);
					marker.setAttributes(map);
					IDE.openEditor(page, marker); // 3.0 API
					marker.delete();
					toFront();
				}
				catch(final Exception e)
				{

				}
			}
		});
	}

	@Override
	public void run()
	{
		final File file = new File(System.getProperty("user.home") + "/apishore-link.txt");
		while(true)
		{
			try
			{
				check(file);
				Thread.sleep(200);
			}
			catch(final Exception e)
			{
				e.printStackTrace();
			}
		}
	}

}
