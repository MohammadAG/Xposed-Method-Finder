package com.mohammadag.xposedmethodfinder;

import java.lang.reflect.Method;

import android.util.Log;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class XposedMod implements IXposedHookLoadPackage, IXposedHookZygoteInit {

	/* Package which has the class methods you want to hook. Zygote hooks don't need this. */
	private static final String PACKAGE_NAME = "";

	/* Classes you want to hook.
	 * Place all the class names you want to hook here. Any class hooked
	 * will cause a logcat message to be printed along with the parameter count,
	 * and basic parameter values.  */
	private static final String[] CLASSES_TO_HOOK = {

	};

	/* Zygote classes go here. */
	private static final String[] ZYGOTE_CLASSES_TO_HOOK = {

	};

	private static final String TAG = "ClassFingerer";

	private static void log(String string) {
		/* Simulate XposedBridge.log, this way we don't rape I/O */
		Log.w("Xposed", String.format("%s: %s", TAG, string));
	}

	/* Generic hook for all the methods */
	private static final XC_MethodHook hook = new XC_MethodHook() {
		@Override
		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
			String paramTypes = "";
			if (param.args.length > 0) {
				for (Object arg : param.args) {
					String paramValue = null;
					if (arg instanceof java.lang.Boolean) {
						Boolean paramBool = (Boolean) arg;
						paramValue = String.valueOf(paramBool);
					}

					if (arg instanceof String) {
						paramValue = (String) arg;
					}
					if (paramValue != null)
						paramTypes += arg.getClass().getName() + ":" + paramValue + ";";
					else
						paramTypes += arg.getClass().getName();
				}
			}
			log(String.format("Class: %s method: %s with parameters of length %s : %s", param.thisObject.getClass().getName(),
					param.method.getName(), String.valueOf(param.args.length), paramTypes));
		}
	};

	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		if (!lpparam.packageName.equals(PACKAGE_NAME))
			return;

		if (CLASSES_TO_HOOK.length == 0)
			return;

		for (String name : CLASSES_TO_HOOK) {
			log("Hooking class " + name);
			Class<?> classToHook = XposedHelpers.findClass(name, lpparam.classLoader);

			Method[] methods = classToHook.getDeclaredMethods();

			for (Method method : methods) {
				XposedBridge.hookMethod(method, hook);
			}
		}
	}

	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
		if (ZYGOTE_CLASSES_TO_HOOK.length == 0)
			return;

		for (String name : ZYGOTE_CLASSES_TO_HOOK) {
			Class<?> classToHook = XposedHelpers.findClass(name, null);
			Method[] methods = classToHook.getDeclaredMethods();

			for (Method method : methods) {
				XposedBridge.hookMethod(method, hook);
			}
		}
	}

}
