/*DO NOT EDIT THIS FILE - it is machine generated*/
package com.badlogic.gdx.controllers;

import apple.corehaptics.CHHapticParameterCurve;
import apple.gamecontroller.GCController.Block_setControllerPausedHandler;
import apple.gamecontroller.GCGamepad.Block_setValueChangedHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.TimeUtils;
import apple.corehaptics.CHHapticEngine;
import apple.corehaptics.CHHapticEvent;
import apple.corehaptics.CHHapticEventParameter;
import apple.corehaptics.CHHapticPattern;
import apple.corehaptics.protocol.CHHapticPatternPlayer;
import apple.foundation.NSArray;
import apple.gamecontroller.GCController;
import apple.gamecontroller.GCControllerAxisInput;
import apple.gamecontroller.GCControllerButtonInput;
import apple.gamecontroller.GCControllerDirectionPad;
import apple.gamecontroller.GCControllerElement;
import apple.gamecontroller.GCExtendedGamepad;
import apple.gamecontroller.GCGamepad;
import java.util.UUID;
import static apple.gamecontroller.enums.GCDeviceBatteryState.*;
import apple.gamecontroller.c.GameController;
import apple.corehaptics.c.CoreHaptics;
import org.moe.natj.objc.ObjCRuntime;
import apple.uikit.UIDevice;

/**
 * DO NOT EDIT THIS FILE - it is machine generated
 */
public class IosController extends AbstractController {

    public static final int BUTTON_BACK = 8;

    public final static int BUTTON_PAUSE = 9;

    public static final int BUTTON_LEFT_STICK = 10;

    public static final int BUTTON_RIGHT_STICK = 11;

    public static final int BUTTON_DPAD_UP = 12;

    public static final int BUTTON_DPAD_DOWN = 13;

    public static final int BUTTON_DPAD_LEFT = 14;

    public static final int BUTTON_DPAD_RIGHT = 15;

    private final GCController controller;

    private final String uuid;

    private final boolean[] pressedButtons;

    private final float[] axisValues;

    private long lastPausePressedMs = 0;

    private CHHapticEngine hapticEngine;

    private CHHapticPatternPlayer playingHapticPattern;

    private long vibrationEndMs;

    public IosController(GCController controller) {
        this.controller = controller;
        uuid = UUID.randomUUID().toString();
        pressedButtons = new boolean[getMaxButtonIndex() + 1];
        axisValues = new float[getAxisCount()];
        ObjCRuntime.retainObject(controller.getPeerPointer());
        if (getMajorSystemVersion() < 13) {
            controller.setControllerPausedHandler(e -> onPauseButtonPressed());
        }
        if (controller.extendedGamepad() != null)
            controller.extendedGamepad().setValueChangedHandler((gamepad, element) -> onControllerValueChanged(element));
        else if (controller.gamepad() != null)
            controller.gamepad().setValueChangedHandler((gamepad, element) -> onControllerValueChanged(element));
        if (getMajorSystemVersion() >= 14)
            try {
                hapticEngine = controller.haptics().createEngineWithLocality(GameController.GCHapticsLocalityDefault());
                ObjCRuntime.retainObject(hapticEngine.getPeerPointer());
            } catch (Throwable t) {
                Gdx.app.error("Controllers", "Failed to create haptics engine", t);
            }
    }

    @Override
    public void dispose() {
        super.dispose();
        controller.setControllerPausedHandler(null);
        if (controller.extendedGamepad() != null)
            controller.extendedGamepad().setValueChangedHandler(null);
        else if (controller.gamepad() != null)
            controller.gamepad().setValueChangedHandler(null);
        ObjCRuntime.releaseObject(controller.getPeerPointer());
        if (hapticEngine != null) {
            ObjCRuntime.releaseObject(hapticEngine.getPeerPointer());
        }
    }

    protected void onPauseButtonPressed() {
        lastPausePressedMs = TimeUtils.millis();
        notifyListenersButtonDown(BUTTON_PAUSE);
        notifyListenersButtonUp(BUTTON_PAUSE);
    }

    protected void onControllerValueChanged(GCControllerElement gcControllerElement) {
        if (gcControllerElement instanceof GCControllerButtonInput) {
            GCControllerButtonInput buttonElement = (GCControllerButtonInput) gcControllerElement;
            boolean pressed = buttonElement.isPressed();
            int buttonNum = getConstFromButtonInput(buttonElement);
            if (buttonNum >= 0 && pressedButtons[buttonNum] != pressed) {
                pressedButtons[buttonNum] = pressed;
                if (pressed)
                    notifyListenersButtonDown(buttonNum);
                else
                    notifyListenersButtonUp(buttonNum);
            }
        } else if (gcControllerElement instanceof GCControllerDirectionPad) {
            // dpad button or axis values changed, cycle to find them all
            for (int buttonNum = BUTTON_DPAD_UP; buttonNum <= BUTTON_DPAD_RIGHT; buttonNum++) {
                GCControllerButtonInput dpadButton = getButtonFromConst(buttonNum);
                if (dpadButton != null && pressedButtons[buttonNum] != dpadButton.isPressed()) {
                    pressedButtons[buttonNum] = dpadButton.isPressed();
                    if (pressedButtons[buttonNum])
                        notifyListenersButtonDown(buttonNum);
                    else
                        notifyListenersButtonUp(buttonNum);
                }
            }
            for (int axisIdx = 0; axisIdx < axisValues.length; axisIdx++) {
                float axisValue = getAxis(axisIdx);
                if (axisValue != axisValues[axisIdx]) {
                    axisValues[axisIdx] = axisValue;
                    notifyListenersAxisMoved(axisIdx, axisValue);
                }
            }
        }
    }

    /**
     * @return constant from button, following W3C recommendations. -1 if not found
     */
    protected int getConstFromButtonInput(GCControllerButtonInput controllerButtonInput) {
        int maxButtonNum = getMaxButtonIndex();
        for (int i = 0; i < maxButtonNum; i++) {
            GCControllerButtonInput buttonFromConst = getButtonFromConst(i);
            if (buttonFromConst != null && controllerButtonInput == buttonFromConst)
                return i;
        }
        if (controllerButtonInput != null)
            Gdx.app.log("Controllers", "Pressed unknown button: " + controllerButtonInput.toString());
        return -1;
    }

    /**
     * @return button from constant, following W3C recommendations
     */
    protected GCControllerButtonInput getButtonFromConst(int i) {
        switch(i) {
            case 0:
                // Button A
                if (controller.extendedGamepad() != null)
                    return controller.extendedGamepad().buttonA();
                else
                    return controller.gamepad().buttonA();
            case 1:
                // Button B
                if (controller.extendedGamepad() != null)
                    return controller.extendedGamepad().buttonB();
                else
                    return controller.gamepad().buttonB();
            case 2:
                // Button X
                if (controller.extendedGamepad() != null)
                    return controller.extendedGamepad().buttonX();
                else
                    return controller.gamepad().buttonX();
            case 3:
                // Button Y
                if (controller.extendedGamepad() != null)
                    return controller.extendedGamepad().buttonY();
                else
                    return controller.gamepad().buttonY();
            case 4:
                // L1
                if (controller.extendedGamepad() != null)
                    return controller.extendedGamepad().leftShoulder();
                else
                    return controller.gamepad().leftShoulder();
            case 5:
                // R1
                if (controller.extendedGamepad() != null)
                    return controller.extendedGamepad().rightShoulder();
                else
                    return controller.gamepad().rightShoulder();
            case 6:
                // L2
                if (controller.extendedGamepad() != null)
                    return controller.extendedGamepad().leftTrigger();
                break;
            case 7:
                // R2
                if (controller.extendedGamepad() != null)
                    return controller.extendedGamepad().rightTrigger();
                break;
            case BUTTON_BACK:
                // Back
                if (getMajorSystemVersion() >= 13 && controller.extendedGamepad() != null) {
                    return controller.extendedGamepad().buttonOptions();
                }
                break;
            case BUTTON_PAUSE:
                // Start
                if (getMajorSystemVersion() >= 13 && controller.extendedGamepad() != null) {
                    return controller.extendedGamepad().buttonMenu();
                }
                break;
            case BUTTON_LEFT_STICK:
                // Left stick button
                if (getMajorSystemVersion() >= 13 && controller.extendedGamepad() != null) {
                    return controller.extendedGamepad().leftThumbstickButton();
                }
                break;
            case BUTTON_RIGHT_STICK:
                // right stick button
                if (getMajorSystemVersion() >= 13 && controller.extendedGamepad() != null) {
                    return controller.extendedGamepad().rightThumbstickButton();
                }
                break;
            case BUTTON_DPAD_UP:
                // Dpad up
                if (controller.extendedGamepad() != null) {
                    return controller.extendedGamepad().dpad().up();
                } else {
                    return controller.gamepad().dpad().up();
                }
            case BUTTON_DPAD_DOWN:
                // dpad down
                if (controller.extendedGamepad() != null) {
                    return controller.extendedGamepad().dpad().down();
                } else {
                    return controller.gamepad().dpad().down();
                }
            case BUTTON_DPAD_LEFT:
                // dpad left
                if (controller.extendedGamepad() != null) {
                    return controller.extendedGamepad().dpad().left();
                } else {
                    return controller.gamepad().dpad().left();
                }
            case BUTTON_DPAD_RIGHT:
                // dpad right
                if (controller.extendedGamepad() != null) {
                    return controller.extendedGamepad().dpad().right();
                } else {
                    return controller.gamepad().dpad().right();
                }
        }
        return null;
    }

    @Override
    public int getMinButtonIndex() {
        return 0;
    }

    @Override
    public int getMaxButtonIndex() {
        return Math.max(BUTTON_DPAD_RIGHT, BUTTON_PAUSE);
    }

    @Override
    public boolean getButton(int i) {
        GCControllerButtonInput buttonFromConst = getButtonFromConst(i);
        if (i == BUTTON_PAUSE && buttonFromConst == null) {
            if (lastPausePressedMs > 0 && (TimeUtils.millis() - lastPausePressedMs) <= 250) {
                lastPausePressedMs = 0;
                return true;
            } else
                return false;
        } else if (buttonFromConst != null)
            return buttonFromConst.isPressed();
        else
            return false;
    }

    protected GCControllerAxisInput getAxisFromConst(int i) {
        switch(i) {
            case 0:
                // Left X
                if (controller.extendedGamepad() != null)
                    return controller.extendedGamepad().leftThumbstick().xAxis();
                break;
            case 1:
                // Left Y
                if (controller.extendedGamepad() != null)
                    return controller.extendedGamepad().leftThumbstick().yAxis();
                break;
            case 2:
                // Right X
                if (controller.extendedGamepad() != null)
                    return controller.extendedGamepad().rightThumbstick().xAxis();
                break;
            case 3:
                // Right Y
                if (controller.extendedGamepad() != null)
                    return controller.extendedGamepad().rightThumbstick().yAxis();
                break;
        }
        return null;
    }

    @Override
    public float getAxis(int i) {
        GCControllerAxisInput axisFromConst = getAxisFromConst(i);
        // we need to flip back vertical axis values, Apple flips it before
        boolean isVertical = i % 2 == 1;
        if (axisFromConst != null)
            return axisFromConst.value() * (isVertical ? -1 : 1);
        return 0;
    }

    @Override
    public String getName() {
        return controller.vendorName();
    }

    @Override
    public String getUniqueId() {
        return uuid;
    }

    @Override
    public boolean supportsPlayerIndex() {
        return true;
    }

    @Override
    public int getPlayerIndex() {
        return (int) controller.playerIndex();
    }

    @Override
    public void setPlayerIndex(int index) {
        controller.setPlayerIndex(index);
    }

    @Override
    public int getAxisCount() {
        return controller.extendedGamepad() != null ? 4 : 0;
    }

    @Override
    public boolean canVibrate() {
        return hapticEngine != null;
    }

    @Override
    public void startVibration(int duration, float strength) {
        if (canVibrate()) {
            try {
                hapticEngine.startAndReturnError(null);
                playingHapticPattern = hapticEngine.createPlayerWithPatternError(constructRumbleEvent((float) duration / 1000, strength), null);
                playingHapticPattern.startAtTimeError(0, null);
                vibrationEndMs = TimeUtils.millis() + duration;
            } catch (Throwable t) {
                Gdx.app.error("Controllers", "Vibration failed", t);
            }
        }
    }

    @Override
    public boolean isVibrating() {
        return canVibrate() && TimeUtils.millis() < vibrationEndMs && playingHapticPattern != null;
    }

    @Override
    public void cancelVibration() {
        if (isVibrating()) {
            playingHapticPattern.cancelAndReturnError(null);
            playingHapticPattern = null;
            vibrationEndMs = 0;
        }
    }

    @Override
    public ControllerMapping getMapping() {
        return MfiMapping.getInstance();
    }

    @Override
    public ControllerPowerLevel getPowerLevel() {
        if (getMajorSystemVersion() >= 14) {
            switch((int) controller.battery().batteryState()) {
                case (int) Discharging:
                    float batteryLevel = controller.battery().batteryLevel();
                    if (batteryLevel <= 0.05f) {
                        return ControllerPowerLevel.POWER_EMPTY;
                    } else if (batteryLevel <= 0.20f) {
                        return ControllerPowerLevel.POWER_LOW;
                    } else if (batteryLevel <= 0.70f) {
                        return ControllerPowerLevel.POWER_MEDIUM;
                    } else {
                        return ControllerPowerLevel.POWER_FULL;
                    }
                case (int) Charging:
                    return ControllerPowerLevel.POWER_WIRED;
                case (int) Full:
                    return ControllerPowerLevel.POWER_FULL;
                default:
                    return ControllerPowerLevel.POWER_UNKNOWN;
            }
        }
        return ControllerPowerLevel.POWER_UNKNOWN;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof IosController && ((IosController) o).getController() == controller);
    }

    public GCController getController() {
        return controller;
    }

    public CHHapticPattern constructRumbleEvent(float length, float strength) {
        NSArray<CHHapticEventParameter> params = (NSArray<CHHapticEventParameter>)NSArray.arrayWithObjects(CHHapticEventParameter.alloc().initWithParameterIDValue(CoreHaptics.CHHapticEventParameterIDHapticIntensity(), strength), CHHapticEventParameter.alloc().initWithParameterIDValue(CoreHaptics.CHHapticEventParameterIDHapticSharpness(), .5f));
        return CHHapticPattern.alloc().initWithEventsParameterCurvesError(
                (NSArray<? extends CHHapticEvent>)NSArray.arrayWithObject(CHHapticEvent.alloc().initWithEventTypeParametersRelativeTimeDuration(CoreHaptics.CHHapticEventTypeHapticContinuous(), params, 0, length)),
                (NSArray<? extends CHHapticParameterCurve>)NSArray.array(), null);
    }

    private static int getMajorSystemVersion() {
        return Integer.parseInt(UIDevice.currentDevice().systemVersion().split("\\.")[0]);
    }
}
