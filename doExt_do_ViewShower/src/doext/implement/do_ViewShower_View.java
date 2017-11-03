package doext.implement;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ViewFlipper;
import core.helper.DoAnimHelper;
import core.helper.DoJsonHelper;
import core.helper.DoUIModuleHelper;
import core.interfaces.DoIScriptEngine;
import core.interfaces.DoIUIModuleView;
import core.object.DoInvokeResult;
import core.object.DoSourceFile;
import core.object.DoUIContainer;
import core.object.DoUIModule;
import doext.define.do_ViewShower_IMethod;
import doext.define.do_ViewShower_MAbstract;

/**
 * 自定义扩展UIView组件实现类，此类必须继承相应VIEW类，并实现DoIUIModuleView,do_SlideView_IMethod接口；
 * #如何调用组件自定义事件？可以通过如下方法触发事件：
 * this.model.getEventCenter().fireEvent(_messageName, jsonResult);
 * 参数解释：@_messageName字符串事件名称，@jsonResult传递事件参数对象；
 * 获取DoInvokeResult对象方式new DoInvokeResult(this.getUniqueKey());
 */
public class do_ViewShower_View extends ViewFlipper implements DoIUIModuleView, do_ViewShower_IMethod, AnimationListener {

	/**
	 * 每个UIview都会引用一个具体的model实例；
	 */
	private do_ViewShower_MAbstract model;
	private Map<String, Integer> viewIndexMap;
	private Map<String, String> viewLoadPathMap;
	private Map<String, String> viewLoadAddressMap;
	private String currViewId;
	private Context mContext;

	public do_ViewShower_View(Context context) {
		super(context);
		this.mContext = context;
		viewIndexMap = new LinkedHashMap<String, Integer>();
		viewLoadPathMap = new LinkedHashMap<String, String>();
		viewLoadAddressMap = new LinkedHashMap<String, String>();
	}

	/**
	 * 初始化加载view准备,_doUIModule是对应当前UIView的model实例
	 */
	@Override
	public void loadView(DoUIModule _doUIModule) throws Exception {
		this.model = (do_ViewShower_MAbstract) _doUIModule;

	}

	/**
	 * 动态修改属性值时会被调用，方法返回值为true表示赋值有效，并执行onPropertiesChanged，否则不进行赋值；
	 * 
	 * @_changedValues<key,value>属性集（key名称、value值）；
	 */
	@Override
	public boolean onPropertiesChanging(Map<String, String> _changedValues) {
		return true;
	}

	/**
	 * 属性赋值成功后被调用，可以根据组件定义相关属性值修改UIView可视化操作；
	 * 
	 * @_changedValues<key,value>属性集（key名称、value值）；
	 */
	@Override
	public void onPropertiesChanged(Map<String, String> _changedValues) {
		DoUIModuleHelper.handleBasicViewProperChanged(this.model, _changedValues);
		// ...do something
	}

	/**
	 * 同步方法，JS脚本调用该组件对象方法时会被调用，可以根据_methodName调用相应的接口实现方法；
	 * 
	 * @_methodName 方法名称
	 * @_dictParas 参数（K,V）
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public boolean invokeSyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		if ("showView".equals(_methodName)) {
			showView(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		if ("addViews".equals(_methodName)) {
			addViews(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		if ("removeView".equals(_methodName)) {
			removeView(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		if ("getView".equals(_methodName)) {
			getView(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		return false;
	}

	/**
	 * 异步方法（通常都处理些耗时操作，避免UI线程阻塞），JS脚本调用该组件对象方法时会被调用， 可以根据_methodName调用相应的接口实现方法；
	 * 
	 * @_methodName 方法名称
	 * @_dictParas 参数（K,V）
	 * @_scriptEngine 当前page JS上下文环境
	 * @_callbackFuncName 回调函数名 #如何执行异步方法回调？可以通过如下方法：
	 *                    _scriptEngine.callback(_callbackFuncName,
	 *                    _invokeResult);
	 *                    参数解释：@_callbackFuncName回调函数名，@_invokeResult传递回调函数参数对象；
	 *                    获取DoInvokeResult对象方式new
	 *                    DoInvokeResult(this.getUniqueKey());
	 */
	@Override
	public boolean invokeAsyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) {
		// ...do something
		return false;
	}

	/**
	 * 释放资源处理，前端JS脚本调用closePage或执行removeui时会被调用；
	 */
	@Override
	public void onDispose() {
		viewIndexMap.clear();
		viewLoadPathMap.clear();
		viewLoadAddressMap.clear();
	}

	/**
	 * 重绘组件，构造组件时由系统框架自动调用；
	 * 或者由前端JS脚本调用组件onRedraw方法时被调用（注：通常是需要动态改变组件（X、Y、Width、Height）属性时手动调用）
	 */
	@Override
	public void onRedraw() {
		this.setLayoutParams(DoUIModuleHelper.getLayoutParams(this.model));
	}

	/**
	 * 获取当前model实例
	 */
	@Override
	public DoUIModule getModel() {
		return model;
	}

	/**
	 * 增加多个View；
	 * 
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public void addViews(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		JSONArray dataArray = DoJsonHelper.getJSONArray(_dictParas, "data");

		for (int i = 0; i < dataArray.length(); i++) {
			JSONObject childData = dataArray.getJSONObject(i);
			String id = DoJsonHelper.getString(childData, "id", "");
			String viewPath = DoJsonHelper.getString(childData, "path", "");
			viewLoadPathMap.put(id, viewPath);

		}
	}

	/**
	 * 删除某个View；
	 * 
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public void removeView(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		String id = DoJsonHelper.getString(_dictParas, "id", "");
		if (viewIndexMap.containsKey(id)) {
			this.removeViewAt(viewIndexMap.get(id));
			viewIndexMap.remove(id);
			viewLoadPathMap.remove(id);
			viewLoadAddressMap.remove(id);
			int size = viewIndexMap.size();
			if (size == 0) {
				return;
			}
			Iterator<Entry<String, Integer>> iterator = viewIndexMap.entrySet().iterator();
			int index = 0;
			while (iterator.hasNext()) {
				Entry<String, Integer> entry = iterator.next();
				if (getDisplayedChild() == index) {
					currViewId = entry.getKey();
				}
				entry.setValue(index++);
			}
		}
	}

	/**
	 * 切换View；
	 * 
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public void showView(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		String id = DoJsonHelper.getString(_dictParas, "id", "");
		if (currViewId != null && currViewId.equals(id)) {
			return;
		}
		currViewId = id;
		String path = viewLoadPathMap.get(currViewId);
		if (path == null) {
			return;
		}
		DoSourceFile _uiFile = _scriptEngine.getCurrentApp().getSourceFS().getSourceByFileName(path);
		if (_uiFile == null) {
			throw new Exception("试图加载一个无效的页面文件:" + path);
		}
		if (!viewIndexMap.containsKey(currViewId)) {
			loadUIView(path, _uiFile);
			viewIndexMap.put(currViewId, getChildCount() - 1);

		}
		String animationType = DoJsonHelper.getString(_dictParas, "animationType", "");
		int animationTime = DoJsonHelper.getInt(_dictParas, "animationTime", 300);
		int anim[] = DoAnimHelper.getAnimGroupResIds(animationType);
		int index = viewIndexMap.get(currViewId);
		if (anim != null) {
			Animation outAnimation = AnimationUtils.loadAnimation(mContext, anim[1]);
			outAnimation.setDuration(animationTime);
			Animation inAnimation = AnimationUtils.loadAnimation(mContext, anim[0]);
			inAnimation.setDuration(animationTime);
			setOutAnimation(outAnimation);
			setInAnimation(inAnimation);
		}
		this.setDisplayedChild(index);
		if (getInAnimation() != null) {
			getInAnimation().setAnimationListener(this);
		} else {
			fireViewChanged();
		}
	}

	private void loadUIView(String path, DoSourceFile dsFile) throws Exception {
		String content = dsFile.getTxtContent();
		DoUIContainer _doUIContainer = new DoUIContainer(model.getCurrentPage());
		_doUIContainer.loadFromContent(content, null, null);
		_doUIContainer.loadDefalutScriptFile(path);
		this.addView((View) _doUIContainer.getRootView().getCurrentUIModuleView());
		viewLoadAddressMap.put(currViewId, _doUIContainer.getRootView().getUniqueKey());
	}

	@Override
	public void onAnimationStart(Animation animation) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAnimationEnd(Animation animation) {
		fireViewChanged();
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		// TODO Auto-generated method stub

	}

	private void fireViewChanged() {
		DoInvokeResult invokeResult = new DoInvokeResult(this.model.getUniqueKey());
		invokeResult.setResultText(currViewId);
		this.model.getEventCenter().fireEvent("viewChanged", invokeResult);
	}

	@Override
	public void getView(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {

		String _id = DoJsonHelper.getString(_dictParas, "id", "");
		if (TextUtils.isEmpty(_id)) {
			throw new Exception("id 参数值不能为空！");
		}
		String _rootViewAddress = viewLoadAddressMap.get(_id);
		_invokeResult.setResultText(_rootViewAddress);

	}
}