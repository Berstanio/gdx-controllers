package com.badlogic.gdx.controllers;

import apple.foundation.NSArray;
import apple.foundation.NSNotification;
import apple.foundation.NSNotificationCenter;
import apple.gamecontroller.GCController;
import apple.uikit.UIKeyCommand;
import apple.uikit.UIViewController;
import apple.uikit.enums.UIKeyModifierFlags;
import apple.usernotifications.c.UserNotifications;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import org.moe.natj.objc.SEL;
import org.moe.natj.objc.ann.Selector;

public class IosControllerManager extends AbstractControllerManager {
	private final Array<ControllerListener> listeners = new Array<>();
	private boolean initialized = false;
	private ICadeController iCadeController;

	public IosControllerManager() {
		listeners.add(new ManageCurrentControllerListener());
	}

	public static void enableICade(UIViewController controller, SEL action) {
		for (int i = 0; i < ICadeController.KEYS_TO_HANDLE.length(); i++) {
			controller.addKeyCommand(UIKeyCommand.keyCommandWithInputModifierFlagsAction(Character.toString(ICadeController.KEYS_TO_HANDLE.charAt(i)), 0, action));
		}

		controller.becomeFirstResponder();
		Gdx.app.log("Controllers", "iCade support activated");
	}

	public static void keyPress(UIKeyCommand sender) {
		//a key for ICadeController was pressed
		// instantiate it, if not already available
		IosControllerManager controllerManager = (IosControllerManager) Controllers.managers.get(Gdx.app);

		if (controllerManager != null)
			controllerManager.handleKeyPressed(sender);
	}

	private void handleKeyPressed(UIKeyCommand sender) {
		if (iCadeController == null) {
			Gdx.app.log("Controllers", "iCade key was pressed, adding iCade controller.");

			iCadeController = new ICadeController();
			controllers.add(iCadeController);

			synchronized (listeners) {
				for (ControllerListener listener : listeners)
					listener.connected(iCadeController);
			}
		}

		iCadeController.handleKeyPressed(sender.input());
	}

	protected boolean isSupportedController(GCController controller) {
		return controller.extendedGamepad() != null || controller.gamepad() != null;
	}

	@Override
	public Array<Controller> getControllers() {
		initializeControllerArray();
		return super.getControllers();
	}

	@Override
	public Controller getCurrentController() {
		initializeControllerArray();
		return super.getCurrentController();
	}

	private void initializeControllerArray() {
		if (!initialized && Utils.getMajorSystemVersion() >= 7) {
			initialized = true;

			NSArray<? extends GCController> controllers = GCController.controllers();

			for (GCController controller : controllers) {
				if (isSupportedController(controller))
					this.controllers.add(new IosController(controller));
			}

			NSNotificationCenter.defaultCenter().addObserverSelectorNameObject(this, new SEL("onControllerConnect"), "GCControllerDidConnectNotification", null);

			NSNotificationCenter.defaultCenter().addObserverSelectorNameObject(this, new SEL("onControllerDisconnect"), "GCControllerDidDisconnectNotification", null);
		}
	}

	@Selector("onControllerConnect")
	public void onControllerConnect(GCController gcController) {
		if (!isSupportedController(gcController))
			return;

		boolean alreadyInList = false;
		for (Controller controller : controllers) {
			if (controller instanceof IosController && ((IosController) controller).getController() == gcController) {
				alreadyInList = true;
				break;
			}
		}

		if (!alreadyInList) {
			IosController iosController = new IosController(gcController);
			controllers.add(iosController);

			synchronized (listeners) {
				for (ControllerListener listener : listeners)
					listener.connected(iosController);
			}
		}
	}

	@Selector("onControllerDisconnect")
	public void onControllerDisconnect(GCController gcController) {
		IosController oldReference = null;
		for (Controller controller : controllers) {
			if (controller instanceof IosController && ((IosController) controller).getController() == gcController) {
				oldReference = (IosController) controller;
			}
		}

		if (oldReference != null) {
			controllers.removeValue(oldReference, true);

			synchronized (listeners) {
				for (ControllerListener listener : listeners)
					listener.disconnected(oldReference);
			}

			oldReference.dispose();
		}
	}

	@Override
	public void addListener(ControllerListener controllerListener) {
		initializeControllerArray();

		synchronized (listeners) {
			if (!listeners.contains(controllerListener, true))
				listeners.add(controllerListener);
		}
	}

	@Override
	public void removeListener(ControllerListener controllerListener) {
		synchronized (listeners) {
			listeners.removeValue(controllerListener, true);
		}
	}

	@Override
	public Array<ControllerListener> getListeners() {
		return listeners;
	}

	@Override
	public void clearListeners() {
		synchronized (listeners) {
			listeners.clear();
			listeners.add(new ManageCurrentControllerListener());
		}
	}
}

