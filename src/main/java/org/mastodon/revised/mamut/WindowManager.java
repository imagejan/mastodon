package org.mastodon.revised.mamut;

import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.mastodon.graph.GraphChangeListener;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.ListenableReadOnlyGraph;
import org.mastodon.revised.bdv.BigDataViewerMaMuT;
import org.mastodon.revised.bdv.SharedBigDataViewerData;
import org.mastodon.revised.bdv.overlay.BdvHighlightHandler;
import org.mastodon.revised.bdv.overlay.BdvSelectionBehaviours;
import org.mastodon.revised.bdv.overlay.EditBehaviours;
import org.mastodon.revised.bdv.overlay.EditSpecialBehaviours;
import org.mastodon.revised.bdv.overlay.OverlayContext;
import org.mastodon.revised.bdv.overlay.OverlayGraphRenderer;
import org.mastodon.revised.bdv.overlay.RenderSettings;
import org.mastodon.revised.bdv.overlay.ui.RenderSettingsManager;
import org.mastodon.revised.bdv.overlay.wrap.OverlayContextWrapper;
import org.mastodon.revised.bdv.overlay.wrap.OverlayEdgeWrapper;
import org.mastodon.revised.bdv.overlay.wrap.OverlayFocusWrapper;
import org.mastodon.revised.bdv.overlay.wrap.OverlayGraphWrapper;
import org.mastodon.revised.bdv.overlay.wrap.OverlayHighlightWrapper;
import org.mastodon.revised.bdv.overlay.wrap.OverlayNavigationWrapper;
import org.mastodon.revised.bdv.overlay.wrap.OverlaySelectionWrapper;
import org.mastodon.revised.bdv.overlay.wrap.OverlayVertexWrapper;
import org.mastodon.revised.context.Context;
import org.mastodon.revised.context.ContextChooser;
import org.mastodon.revised.context.ContextListener;
import org.mastodon.revised.context.ContextProvider;
import org.mastodon.revised.model.mamut.BoundingSphereRadiusStatistics;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.ModelOverlayProperties;
import org.mastodon.revised.model.mamut.Spot;
import org.mastodon.revised.trackscheme.DefaultModelFocusProperties;
import org.mastodon.revised.trackscheme.DefaultModelGraphProperties;
import org.mastodon.revised.trackscheme.DefaultModelHighlightProperties;
import org.mastodon.revised.trackscheme.DefaultModelNavigationProperties;
import org.mastodon.revised.trackscheme.DefaultModelSelectionProperties;
import org.mastodon.revised.trackscheme.ModelFocusProperties;
import org.mastodon.revised.trackscheme.ModelHighlightProperties;
import org.mastodon.revised.trackscheme.ModelNavigationProperties;
import org.mastodon.revised.trackscheme.ModelSelectionProperties;
import org.mastodon.revised.trackscheme.TrackSchemeContextListener;
import org.mastodon.revised.trackscheme.TrackSchemeFocus;
import org.mastodon.revised.trackscheme.TrackSchemeGraph;
import org.mastodon.revised.trackscheme.TrackSchemeHighlight;
import org.mastodon.revised.trackscheme.TrackSchemeNavigation;
import org.mastodon.revised.trackscheme.TrackSchemeSelection;
import org.mastodon.revised.trackscheme.display.DefaultTrackSchemeOverlay;
import org.mastodon.revised.trackscheme.display.TrackSchemeEditBehaviours;
import org.mastodon.revised.trackscheme.display.TrackSchemeFrame;
import org.mastodon.revised.trackscheme.display.TrackSchemeOptions;
import org.mastodon.revised.trackscheme.display.style.TrackSchemeStyle;
import org.mastodon.revised.trackscheme.display.style.TrackSchemeStyle.UpdateListener;
import org.mastodon.revised.trackscheme.display.style.TrackSchemeStyleManager;
import org.mastodon.revised.ui.HighlightBehaviours;
import org.mastodon.revised.ui.SelectionActions;
import org.mastodon.revised.ui.grouping.GroupHandle;
import org.mastodon.revised.ui.grouping.GroupManager;
import org.mastodon.revised.ui.selection.FocusListener;
import org.mastodon.revised.ui.selection.FocusModel;
import org.mastodon.revised.ui.selection.HighlightListener;
import org.mastodon.revised.ui.selection.HighlightModel;
import org.mastodon.revised.ui.selection.NavigationHandler;
import org.mastodon.revised.ui.selection.Selection;
import org.mastodon.revised.ui.selection.SelectionListener;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.io.InputTriggerDescription;
import org.scijava.ui.behaviour.io.InputTriggerDescriptionsBuilder;
import org.scijava.ui.behaviour.io.yaml.YamlConfigIO;
import org.scijava.ui.behaviour.util.AbstractNamedAction;

import bdv.spimdata.SpimDataMinimal;
import bdv.viewer.RequestRepaint;
import bdv.viewer.TimePointListener;
import bdv.viewer.ViewerFrame;
import bdv.viewer.ViewerOptions;
import bdv.viewer.ViewerPanel;
import mpicbg.spim.data.generic.AbstractSpimData;

public class WindowManager
{
	/**
	 * Information for one BigDataViewer window.
	 */
	public static class BdvWindow
	{
		private final ViewerFrame viewerFrame;

		private final OverlayGraphRenderer< ?, ? > tracksOverlay;

		private final GroupHandle groupHandle;

		private final ContextProvider< Spot > contextProvider;

		public BdvWindow(
				final ViewerFrame viewerFrame,
				final OverlayGraphRenderer< ?, ? > tracksOverlay,
				final GroupHandle groupHandle,
				final ContextProvider< Spot > contextProvider )
		{
			this.viewerFrame = viewerFrame;
			this.tracksOverlay = tracksOverlay;
			this.groupHandle = groupHandle;
			this.contextProvider = contextProvider;
		}

		public ViewerFrame getViewerFrame()
		{
			return viewerFrame;
		}

		public OverlayGraphRenderer< ?, ? > getTracksOverlay()
		{
			return tracksOverlay;
		}

		public GroupHandle getGroupHandle()
		{
			return groupHandle;
		}

		public ContextProvider< Spot > getContextProvider()
		{
			return contextProvider;
		}
	}

	/**
	 * Information for one TrackScheme window.
	 */
	public static class TsWindow
	{
		private final TrackSchemeFrame trackSchemeFrame;

		private final GroupHandle groupHandle;

		private final ContextChooser< Spot > contextChooser;

		public TsWindow(
				final TrackSchemeFrame trackSchemeFrame,
				final GroupHandle groupHandle,
				final ContextChooser< Spot > contextChooser )
		{
			this.trackSchemeFrame = trackSchemeFrame;
			this.groupHandle = groupHandle;
			this.contextChooser = contextChooser;
		}

		public TrackSchemeFrame getTrackSchemeFrame()
		{
			return trackSchemeFrame;
		}

		public GroupHandle getGroupHandle()
		{
			return groupHandle;
		}

		public ContextChooser< Spot > getContextChooser()
		{
			return contextChooser;
		}
	}

	/**
	 * TODO!!! related to {@link OverlayContextWrapper}
	 *
	 * @param <V>
	 *            the type of vertices in the model.
	 *
	 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
	 */
	public static class BdvContextAdapter< V > implements ContextListener< V >, ContextProvider< V >
	{
		private final String contextProviderName;

		private final ArrayList< ContextListener< V > > listeners;

		private Context< V > context;

		public BdvContextAdapter( final String contextProviderName )
		{
			this.contextProviderName = contextProviderName;
			listeners = new ArrayList<>();
		}

		@Override
		public String getContextProviderName()
		{
			return contextProviderName;
		}

		@Override
		public synchronized boolean addContextListener( final ContextListener< V > l )
		{
			if ( !listeners.contains( l ) )
			{
				listeners.add( l );
				l.contextChanged( context );
				return true;
			}
			return false;
		}

		@Override
		public synchronized boolean removeContextListener( final ContextListener< V > l )
		{
			return listeners.remove( l );
		}

		@Override
		public synchronized void contextChanged( final Context< V > context )
		{
			this.context = context;
			for ( final ContextListener< V > l : listeners )
				l.contextChanged( context );
		}
	}

	private final Model model;

	private final InputTriggerConfig keyconf;

	private final GroupManager groupManager;

	private final SharedBigDataViewerData sharedBdvData;

	private final int minTimepoint;

	private final int maxTimepoint;

	private final Selection< Spot, Link > selection;

	private final HighlightModel< Spot, Link > highlightModel;

	private final FocusModel< Spot, Link > focusModel;

	private final BoundingSphereRadiusStatistics radiusStats;

	/**
	 * All currently open BigDataViewer windows.
	 */
	private final List< BdvWindow > bdvWindows = new ArrayList<>();

	/**
	 * The {@link ContextProvider}s of all currently open BigDataViewer windows.
	 */
	private final List< ContextProvider< Spot > > contextProviders = new ArrayList<>();

	/**
	 * All currently open TrackScheme windows.
	 */
	private final List< TsWindow > tsWindows = new ArrayList<>();

	private final RenderSettingsManager renderSettingsManager;

	private final TrackSchemeStyleManager trackSchemeStyleManager;

	public WindowManager(
			final String spimDataXmlFilename,
			final SpimDataMinimal spimData,
			final Model model,
			final RenderSettingsManager bdvSettingsManager,
			final TrackSchemeStyleManager trackSchemeStyleManager,
			final InputTriggerConfig keyconf )
	{
		this.model = model;
		this.renderSettingsManager = bdvSettingsManager;
		this.trackSchemeStyleManager = trackSchemeStyleManager;
		this.keyconf = keyconf;

		groupManager = new GroupManager();
		final RequestRepaint requestRepaint = new RequestRepaint()
		{
			@Override
			public void requestRepaint()
			{
				for ( final BdvWindow w : bdvWindows )
					w.getViewerFrame().getViewerPanel().requestRepaint();
			}
		};
		sharedBdvData = new SharedBigDataViewerData( spimDataXmlFilename, spimData, ViewerOptions.options().inputTriggerConfig( keyconf ), requestRepaint );

		final ListenableReadOnlyGraph< Spot, Link > graph = model.getGraph();
		final GraphIdBimap< Spot, Link > idmap = model.getGraphIdBimap();
		selection = new Selection<>( graph, idmap );
		highlightModel = new HighlightModel<  >( idmap );
		radiusStats = new BoundingSphereRadiusStatistics( model );
		focusModel = new FocusModel<>( idmap );

		minTimepoint = 0;
		maxTimepoint = sharedBdvData.getNumTimepoints() - 1;
		/*
		 * TODO: (?) For now, we use timepoint indices in MaMuT model, instead
		 * of IDs/names. This is because BDV also displays timepoint index, and
		 * it would be confusing to have different labels in TrackScheme. If
		 * this is changed in the future, then probably only in the model files.
		 */

	}

	private synchronized void addBdvWindow( final BdvWindow w )
	{
		w.getViewerFrame().addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosing( final WindowEvent e )
			{
				removeBdvWindow( w );
			}
		} );
		bdvWindows.add( w );
		contextProviders.add( w.getContextProvider() );
		for ( final TsWindow tsw : tsWindows )
			tsw.getContextChooser().updateContextProviders( contextProviders );
	}

	private synchronized void removeBdvWindow( final BdvWindow w )
	{
		bdvWindows.remove( w );
		contextProviders.remove( w.getContextProvider() );
		for ( final TsWindow tsw : tsWindows )
			tsw.getContextChooser().updateContextProviders( contextProviders );
	}

	private synchronized void addTsWindow( final TsWindow w )
	{
		w.getTrackSchemeFrame().addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosing( final WindowEvent e )
			{
				removeTsWindow( w );
			}
		} );
		tsWindows.add( w );
		w.getContextChooser().updateContextProviders( contextProviders );
	}

	private synchronized void removeTsWindow( final TsWindow w )
	{
		tsWindows.remove( w );
		w.getContextChooser().updateContextProviders( new ArrayList<>() );
	}

	// TODO
	private int bdvName = 1;

	public void createBigDataViewer()
	{
		final GroupHandle bdvGroupHandle = groupManager.createGroupHandle();

		final OverlayGraphWrapper< Spot, Link > overlayGraph = new OverlayGraphWrapper<>(
				model.getGraph(),
				model.getGraphIdBimap(),
				model.getSpatioTemporalIndex(),
				new ModelOverlayProperties( model.getGraph(), radiusStats, selection ) );

		final OverlayHighlightWrapper< Spot, Link > overlayHighlight = new OverlayHighlightWrapper<>(
				model.getGraphIdBimap(),
				highlightModel );

		final OverlayFocusWrapper< Spot, Link > overlayFocus = new OverlayFocusWrapper<>(
				model.getGraphIdBimap(),
				focusModel );

		final OverlaySelectionWrapper< Spot, Link > overlaySelection = new OverlaySelectionWrapper<>(
				selection );

		final String windowTitle = "BigDataViewer " + (bdvName++); // TODO: use JY naming scheme
		final BigDataViewerMaMuT bdv = BigDataViewerMaMuT.open( sharedBdvData, windowTitle, bdvGroupHandle );
		final ViewerFrame viewerFrame = bdv.getViewerFrame();
		final ViewerPanel viewer = bdv.getViewer();


		// TODO: It's ok to create the wrappers here, but wiring up Listeners should be done elsewhere


//		if ( !bdv.tryLoadSettings( bdvFile ) ) // TODO
//			InitializeViewerState.initBrightness( 0.001, 0.999, bdv.getViewer(), bdv.getSetupAssignments() );

		viewer.setTimepoint( currentTimepoint );
		final OverlayGraphRenderer< OverlayVertexWrapper< Spot, Link >, OverlayEdgeWrapper< Spot, Link > > tracksOverlay = new OverlayGraphRenderer<>(
				overlayGraph,
				overlayHighlight,
				overlayFocus );
		viewer.getDisplay().addOverlayRenderer( tracksOverlay );
		viewer.addRenderTransformListener( tracksOverlay );
		viewer.addTimePointListener( tracksOverlay );
		overlayHighlight.addHighlightListener( new HighlightListener()
		{
			@Override
			public void highlightChanged()
			{
				viewer.getDisplay().repaint();
			}
		} );
		overlayFocus.addFocusListener( new FocusListener()
		{
			@Override
			public void focusChanged()
			{
				viewer.getDisplay().repaint();
			}
		} );
		model.getGraph().addGraphChangeListener( new GraphChangeListener()
		{
			@Override
			public void graphChanged()
			{
				viewer.getDisplay().repaint();
			}
		} );
		model.getGraph().addVertexPositionListener( ( v ) -> viewer.getDisplay().repaint() );
		overlaySelection.addSelectionListener( new SelectionListener()
		{
			@Override
			public void selectionChanged()
			{
				viewer.getDisplay().repaint();
			}
		} );
		// TODO: remember those listeners and remove them when the BDV window is closed!!!

		final BdvHighlightHandler< ?, ? > highlightHandler = new BdvHighlightHandler<>( overlayGraph, tracksOverlay, overlayHighlight );
		viewer.getDisplay().addHandler( highlightHandler );
		viewer.addRenderTransformListener( highlightHandler );

		final NavigationHandler< Spot, Link > navigationHandler = new NavigationHandler<>( bdvGroupHandle );
		final OverlayNavigationWrapper< Spot, Link > navigation =
				new OverlayNavigationWrapper< >( viewer, overlayGraph, navigationHandler );
		final BdvSelectionBehaviours< ?, ? > selectionBehaviours = new BdvSelectionBehaviours<>( overlayGraph, tracksOverlay, overlaySelection, navigation );
		selectionBehaviours.installBehaviourBindings( viewerFrame.getTriggerbindings(), keyconf );

		final OverlayContext< OverlayVertexWrapper< Spot, Link > > overlayContext = new OverlayContext<>( overlayGraph, tracksOverlay );
		viewer.addRenderTransformListener( overlayContext );
		final BdvContextAdapter< Spot > contextProvider = new BdvContextAdapter<>( windowTitle );
		final OverlayContextWrapper< Spot, Link > overlayContextWrapper = new OverlayContextWrapper<>(
				overlayContext,
				contextProvider );

		UndoActions.installActionBindings( viewerFrame.getKeybindings(), model, keyconf );
		EditBehaviours.installActionBindings( viewerFrame.getTriggerbindings(), keyconf, overlayGraph, tracksOverlay, model );
		EditSpecialBehaviours.installActionBindings( viewerFrame.getTriggerbindings(), keyconf, viewerFrame.getViewerPanel(), overlayGraph, tracksOverlay, model );
		HighlightBehaviours.installActionBindings(
				viewerFrame.getTriggerbindings(),
				keyconf,
				new String[] {"bdv"},
				model.getGraph(),
				model.getGraph(),
				highlightModel,
				model );
		SelectionActions.installActionBindings(
				viewerFrame.getKeybindings(),
				keyconf,
				new String[] { "bdv" },
				model.getGraph(),
				model.getGraph(),
				selection,
				model );

		/*
		 * TODO: this is still wrong. There should be one central entity syncing
		 * time for several BDV frames and TrackSchemePanel should listen to
		 * that. Ideally windows should be configurable to "share" timepoints or
		 * not.
		 */
		viewer.addTimePointListener( tpl );

		/*
		 * BDV menu.
		 */

		final BdvRenderSettingsUpdater panelRepainter = new BdvRenderSettingsUpdater( tracksOverlay, viewer );
		final JMenu styleMenu = new JMenu( "Styles" );
		styleMenu.addMenuListener( new MenuListener()
		{
			@Override
			public void menuSelected( final MenuEvent e )
			{
				styleMenu.removeAll();
				for ( final RenderSettings rs : renderSettingsManager.getRenderSettings() )
					styleMenu.add( new JMenuItem(
							new BdvRenderSettingsAction( rs, panelRepainter ) ) );

			}

			@Override public void menuDeselected( final MenuEvent e ) {}
			@Override public void menuCanceled( final MenuEvent e ) {}
		} );
		viewerFrame.getJMenuBar().add( styleMenu );

		/*
		 * De-register render settings listener upon window closing.
		 */
		viewerFrame.addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosing( final WindowEvent e )
			{
				for ( final RenderSettings rs : renderSettingsManager.getRenderSettings() )
					rs.removeUpdateListener( panelRepainter );
			};
		} );


		final BdvWindow bdvWindow = new BdvWindow( viewerFrame, tracksOverlay, bdvGroupHandle, contextProvider );
		addBdvWindow( bdvWindow );
	}

	private class BdvRenderSettingsUpdater implements org.mastodon.revised.bdv.overlay.RenderSettings.UpdateListener
	{

		private final ViewerPanel viewer;

		private final OverlayGraphRenderer< ?, ? > overlay;

		private RenderSettings renderSettings;

		public BdvRenderSettingsUpdater( final OverlayGraphRenderer< ?, ? > overlay, final ViewerPanel viewer )
		{
			this.overlay = overlay;
			this.viewer = viewer;
		}

		@Override
		public void renderSettingsChanged()
		{
			overlay.setRenderSettings( renderSettings );
			viewer.repaint();
		}
	}

	/**
	 * Sets the style of a TrackScheme panel when executed & registers itself as
	 * a listener for style changes to repaint said panel.
	 */
	private class BdvRenderSettingsAction extends AbstractNamedAction
	{

		private static final long serialVersionUID = 1L;

		private final RenderSettings rs;

		private final BdvRenderSettingsUpdater panelRepainter;

		public BdvRenderSettingsAction( final RenderSettings rs, final BdvRenderSettingsUpdater panelRepainter )
		{
			super( rs.getName() );
			this.rs = rs;
			this.panelRepainter = panelRepainter;
		}

		@Override
		public void actionPerformed( final ActionEvent e )
		{
			panelRepainter.renderSettings = rs;
			rs.addUpdateListener( panelRepainter );
			panelRepainter.renderSettingsChanged();
		}
	}

	// TODO testing only
	private int currentTimepoint = 0;

	// TODO testing only
	private final TimePointListener tpl = new TimePointListener()
	{
		@Override
		public void timePointChanged( final int timePointIndex )
		{
			if ( currentTimepoint != timePointIndex )
			{
				currentTimepoint = timePointIndex;
				for ( final TsWindow w : tsWindows )
					w.getTrackSchemeFrame().getTrackschemePanel().timePointChanged( timePointIndex );
			}
		}
	};

	public void createTrackScheme()
	{
		final ListenableReadOnlyGraph< Spot, Link > graph = model.getGraph();
		final GraphIdBimap< Spot, Link > idmap = model.getGraphIdBimap();

		/*
		 * TrackSchemeGraph listening to model
		 */
		final DefaultModelGraphProperties< Spot, Link > properties = new DefaultModelGraphProperties<>( graph, idmap, selection );
		final TrackSchemeGraph< Spot, Link > trackSchemeGraph = new TrackSchemeGraph<>( graph, idmap, properties );

		/*
		 * TrackSchemeHighlight wrapping HighlightModel
		 */
		final ModelHighlightProperties highlightProperties = new DefaultModelHighlightProperties< >( graph, idmap, highlightModel );
		final TrackSchemeHighlight trackSchemeHighlight = new TrackSchemeHighlight( highlightProperties, trackSchemeGraph );

		/*
		 * TrackScheme selection
		 */
		final ModelSelectionProperties selectionProperties = new DefaultModelSelectionProperties< >( graph, idmap, selection );
		final TrackSchemeSelection trackSchemeSelection = new TrackSchemeSelection( selectionProperties );

		/*
		 * TrackScheme GroupHandle
		 */
		final GroupHandle groupHandle = groupManager.createGroupHandle();

		/*
		 * TrackScheme navigation
		 */
		final NavigationHandler< Spot, Link > navigationHandler = new NavigationHandler<>( groupHandle );
		final ModelNavigationProperties navigationProperties = new DefaultModelNavigationProperties< >( graph, idmap, navigationHandler );
		final TrackSchemeNavigation trackSchemeNavigation = new TrackSchemeNavigation( navigationProperties, trackSchemeGraph );

		/*
		 * TrackScheme focus
		 */
		final ModelFocusProperties focusProperties = new DefaultModelFocusProperties<>( graph, idmap, focusModel );
		final TrackSchemeFocus trackSchemeFocus = new TrackSchemeFocus( focusProperties, trackSchemeGraph );

		/*
		 * TrackScheme ContextChooser
		 */
		final TrackSchemeContextListener< Spot > contextListener = new TrackSchemeContextListener< >(
				idmap,
				trackSchemeGraph );
		final ContextChooser< Spot > contextChooser = new ContextChooser<>( contextListener );

		/*
		 * show TrackSchemeFrame
		 */
		final TrackSchemeFrame frame = new TrackSchemeFrame(
				trackSchemeGraph,
				trackSchemeHighlight,
				trackSchemeFocus,
				trackSchemeSelection,
				trackSchemeNavigation,
				model,
				groupHandle,
				contextChooser,
				TrackSchemeOptions.options().inputTriggerConfig( keyconf ) );
		frame.getTrackschemePanel().setTimepointRange( minTimepoint, maxTimepoint );
		frame.getTrackschemePanel().graphChanged();
		contextListener.setContextListener( frame.getTrackschemePanel() );
		frame.setVisible( true );

		UndoActions.installActionBindings( frame.getKeybindings(), model, keyconf );
		HighlightBehaviours.installActionBindings(
				frame.getTriggerbindings(),
				keyconf,
				new String[] { "ts" },
				model.getGraph(),
				model.getGraph(),
				highlightModel,
				model );
		SelectionActions.installActionBindings(
				frame.getKeybindings(),
				keyconf,
				new String[] { "ts" },
				model.getGraph(),
				model.getGraph(),
				selection,
				model );
		TrackSchemeEditBehaviours.installActionBindings(
				frame.getTriggerbindings(),
				keyconf,
				frame.getTrackschemePanel(),
				trackSchemeGraph,
				frame.getTrackschemePanel().getGraphOverlay(),
				model.getGraph(),
				model.getGraph().getGraphIdBimap(),
				model );

		/*
		 * TrackScheme menu
		 */
		final JMenuBar menu = new JMenuBar();

		// Styles auto-populated from TrackScheme style manager.
		if ( frame.getTrackschemePanel().getGraphOverlay() instanceof DefaultTrackSchemeOverlay )
		{
			// Update listener that repaint this TrackScheme when its style changes
			final UpdateListener panelRepainter = new UpdateListener() 
				{ @Override public void trackSchemeStyleChanged() { frame.getTrackschemePanel().repaint(); } };

			final DefaultTrackSchemeOverlay overlay = ( DefaultTrackSchemeOverlay ) frame.getTrackschemePanel().getGraphOverlay();
			final JMenu styleMenu = new JMenu( "Styles" );

			styleMenu.addMenuListener( new MenuListener()
			{
				@Override
				public void menuSelected( final MenuEvent e )
				{
					styleMenu.removeAll();
					for ( final TrackSchemeStyle style : trackSchemeStyleManager.getStyles() )
						styleMenu.add( new JMenuItem(
								new TrackSchemeStyleAction( style, overlay, panelRepainter ) ) );
				}

				@Override
				public void menuDeselected( final MenuEvent e )
				{}

				@Override
				public void menuCanceled( final MenuEvent e )
				{}
			} );
			/*
			 * De-register style listener upon window closing.
			 */
			frame.addWindowListener( new WindowAdapter()
			{
				@Override
				public void windowClosing( final WindowEvent e )
				{
					for ( final TrackSchemeStyle style : trackSchemeStyleManager.getStyles() )
						style.removeUpdateListener( panelRepainter );
				};
			} );

			menu.add( styleMenu );
		}
		frame.setJMenuBar( menu );


		final TsWindow tsWindow = new TsWindow( frame, groupHandle, contextChooser );
		addTsWindow( tsWindow );
		frame.getTrackschemePanel().repaint();
	}

	/**
	 * Sets the style of a TrackScheme panel when executed & registers itself as
	 * a listener for style changes to repaint said panel.
	 */
	private class TrackSchemeStyleAction extends AbstractNamedAction
	{

		private static final long serialVersionUID = 1L;

		private final TrackSchemeStyle style;

		private final UpdateListener panelRepainter;

		private final DefaultTrackSchemeOverlay overlay;

		public TrackSchemeStyleAction( final TrackSchemeStyle style, final DefaultTrackSchemeOverlay overlay, final UpdateListener panelRepainter )
		{
			super( style.name );
			this.style = style;
			this.overlay = overlay;
			this.panelRepainter = panelRepainter;
		}

		@Override
		public void actionPerformed( final ActionEvent e )
		{
			final TrackSchemeStyle oldStyle = overlay.setStyle( style );
			oldStyle.removeUpdateListener( panelRepainter );
			style.addUpdateListener( panelRepainter );
			panelRepainter.trackSchemeStyleChanged();
		}
	}

	public void closeAllWindows()
	{
		final ArrayList< JFrame > frames = new ArrayList<>();
		for ( final BdvWindow w : bdvWindows )
			frames.add( w.getViewerFrame() );
		for ( final TsWindow w : tsWindows )
			frames.add( w.getTrackSchemeFrame() );
		SwingUtilities.invokeLater( new Runnable()
		{
			@Override
			public void run()
			{
				for ( final JFrame f : frames )
					f.dispatchEvent( new WindowEvent( f, WindowEvent.WINDOW_CLOSING ) );
			}
		} );
	}

	public Model getModel()
	{
		return model;
	}

	public AbstractSpimData< ? > getSpimData()
	{
		return sharedBdvData.getSpimData();
	}

	public RenderSettingsManager getBDVSettingsManager()
	{
		return renderSettingsManager;
	}


	// TODO: move somewhere else. make bdvWindows, tsWindows accessible.
	public static class DumpInputConfig
	{
		private static List< InputTriggerDescription > buildDescriptions( final WindowManager wm ) throws IOException
		{
			final InputTriggerDescriptionsBuilder builder = new InputTriggerDescriptionsBuilder();

			final ViewerFrame viewerFrame = wm.bdvWindows.get( 0 ).viewerFrame;
			builder.addMap( viewerFrame.getKeybindings().getConcatenatedInputMap(), "bdv" );
			builder.addMap( viewerFrame.getTriggerbindings().getConcatenatedInputTriggerMap(), "bdv" );

			final TrackSchemeFrame trackschemeFrame = wm.tsWindows.get( 0 ).trackSchemeFrame;
			builder.addMap( trackschemeFrame.getKeybindings().getConcatenatedInputMap(), "ts" );
			builder.addMap( trackschemeFrame.getTriggerbindings().getConcatenatedInputTriggerMap(), "ts" );

			return builder.getDescriptions();
		}

		public static boolean mkdirs( final String fileName )
		{
			final File dir = new File( fileName ).getParentFile();
			return dir == null ? false : dir.mkdirs();
		}

		public static void writeToYaml( final String fileName, final WindowManager wm ) throws IOException
		{
			mkdirs( fileName );
			YamlConfigIO.write(  buildDescriptions( wm ), fileName );
		}
	}
}
