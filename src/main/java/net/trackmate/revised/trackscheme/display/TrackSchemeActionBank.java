package net.trackmate.revised.trackscheme.display;

import java.awt.event.ActionEvent;

import bdv.util.AbstractNamedAction;

public class TrackSchemeActionBank
{

	/*
	 * NAVIGATE WITH SELECTION.
	 */

	public static final AbstractNamedAction getNavigateToChildAction( final HighlightNavigator selectionNavigator )
	{
		return new AbstractNamedAction( "navigateToChild" )
		{
			private static final long serialVersionUID = 2501943103873265924L;

			@Override
			public void actionPerformed( final ActionEvent event )
			{
				new Thread( this.name() )
				{
					@Override
					public void run()
					{
						selectionNavigator.child();
					}
				}.start();
			}
		};
	}

	public static final AbstractNamedAction getNavigateToParentAction( final HighlightNavigator selectionNavigator )
	{
		return new AbstractNamedAction( "navigateToParent" )
		{
			private static final long serialVersionUID = -2192196689286789410L;

			@Override
			public void actionPerformed( final ActionEvent event )
			{
				new Thread( this.name() )
				{
					@Override
					public void run()
					{
						selectionNavigator.parent();
					}
				}.start();
			}
		};
	}

	public static final AbstractNamedAction getNavigateToRightSiblingAction( final HighlightNavigator selectionNavigator )
	{
		return new AbstractNamedAction( "navigateToRightSibling" )
		{
			private static final long serialVersionUID = -357160008620454907L;

			@Override
			public void actionPerformed( final ActionEvent event )
			{
				new Thread( this.name() )
				{
					@Override
					public void run()
					{
						selectionNavigator.rightSibling();
					}
				}.start();
			}
		};
	}

	public static final AbstractNamedAction getNavigateToLeftSiblingAction( final HighlightNavigator selectionNavigator )
	{
		return new AbstractNamedAction( "navigateToLeftSibling" )
		{
			private static final long serialVersionUID = -8829626037712844926L;

			@Override
			public void actionPerformed( final ActionEvent event )
			{
				new Thread( this.name() )
				{
					@Override
					public void run()
					{
						selectionNavigator.leftSibling();
					}
				}.start();
			}
		};
	}

	public static final AbstractNamedAction getAddChildToSelectionAction( final HighlightNavigator selectionNavigator )
	{
		return new AbstractNamedAction( "addChildToSelection" )
		{
			private static final long serialVersionUID = 3028016888295534595L;

			@Override
			public void actionPerformed( final ActionEvent event )
			{
				new Thread( this.name() )
				{
					@Override
					public void run()
					{
						selectionNavigator.child();
					}
				}.start();
			}
		};
	}

	public static final AbstractNamedAction getAddParentToSelectionAction( final HighlightNavigator selectionNavigator )
	{
		return new AbstractNamedAction( "addParentToSelection" )
		{
			private static final long serialVersionUID = -454383452845176573L;

			@Override
			public void actionPerformed( final ActionEvent event )
			{
				new Thread( this.name() )
				{
					@Override
					public void run()
					{
						selectionNavigator.parent();
					}
				}.start();
			}
		};
	}

	public static final AbstractNamedAction getAddRightSiblingToSelectionAction( final HighlightNavigator selectionNavigator )
	{
		return new AbstractNamedAction( "addRightSiblingToSelection" )
		{
			private static final long serialVersionUID = 6379431524819061875L;

			@Override
			public void actionPerformed( final ActionEvent event )
			{
				new Thread( this.name() )
				{
					@Override
					public void run()
					{
						selectionNavigator.rightSibling();
					}
				}.start();
			}
		};
	}

	public static final AbstractNamedAction getAddLeftSiblingToSelectionAction( final HighlightNavigator selectionNavigator )
	{
		return new AbstractNamedAction( "addLeftSiblingToSelection" )
		{
			private static final long serialVersionUID = -6558360734713816879L;

			@Override
			public void actionPerformed( final ActionEvent event )
			{
				new Thread( this.name() )
				{
					@Override
					public void run()
					{
						selectionNavigator.leftSibling();
					}
				}.start();
			}
		};
	}

	private TrackSchemeActionBank()
	{}
}
