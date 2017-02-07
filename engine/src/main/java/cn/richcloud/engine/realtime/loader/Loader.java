package cn.richcloud.engine.realtime.loader;

import cn.richcloud.common.tool.AnalysisOptions;
import cn.richcloud.engine.realtime.common.utils.RealResult;

public abstract class Loader {

	public abstract RealResult load(AnalysisOptions options) ;
}
