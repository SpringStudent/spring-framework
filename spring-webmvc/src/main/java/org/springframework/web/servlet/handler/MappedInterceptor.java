/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.web.servlet.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.ObjectUtils;
import org.springframework.util.PathMatcher;
import org.springframework.web.context.request.WebRequestInterceptor;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * 包含并委托对{@link HandlerInterceptor}的调用以及拦截器应该应用的包含（和可选地排除）路径模式。
 * 还提供匹配逻辑以测试拦截器是否适用于给定的请求路径。
 *
 * <p>A MappedInterceptor can be registered directly with any
 * {@link org.springframework.web.servlet.handler.AbstractHandlerMethodMapping}.
 * Furthermore, beans of type {@code MappedInterceptor} are automatically detected by
 * {@code AbstractHandlerMethodMapping} (including ancestor ApplicationContext's) which
 * effectively means the interceptor is registered "globally" with all handler mappings.
 *
 * @author Keith Donald
 * @author Rossen Stoyanchev
 * @author Brian Clozel
 * @since 3.0
 */
public final class MappedInterceptor implements HandlerInterceptor {

	private final String[] includePatterns;

	private final String[] excludePatterns;

	private final HandlerInterceptor interceptor;

	private PathMatcher pathMatcher;


	/**
	 * Create a new MappedInterceptor instance.
	 * @param includePatterns the path patterns to map (empty for matching to all paths)
	 * @param interceptor the HandlerInterceptor instance to map to the given patterns
	 */
	public MappedInterceptor(String[] includePatterns, HandlerInterceptor interceptor) {
		this(includePatterns, null, interceptor);
	}

	/**
	 * Create a new MappedInterceptor instance.
	 * @param includePatterns the path patterns to map (empty for matching to all paths)
	 * @param excludePatterns the path patterns to exclude (empty for no specific excludes)
	 * @param interceptor the HandlerInterceptor instance to map to the given patterns
	 */
	public MappedInterceptor(String[] includePatterns, String[] excludePatterns, HandlerInterceptor interceptor) {
		this.includePatterns = includePatterns;
		this.excludePatterns = excludePatterns;
		this.interceptor = interceptor;
	}


	/**
	 * Create a new MappedInterceptor instance.
	 * @param includePatterns the path patterns to map (empty for matching to all paths)
	 * @param interceptor the WebRequestInterceptor instance to map to the given patterns
	 */
	public MappedInterceptor(String[] includePatterns, WebRequestInterceptor interceptor) {
		this(includePatterns, null, interceptor);
	}

	/**
	 * Create a new MappedInterceptor instance.
	 * @param includePatterns the path patterns to map (empty for matching to all paths)
	 * @param excludePatterns the path patterns to exclude (empty for no specific excludes)
	 * @param interceptor the WebRequestInterceptor instance to map to the given patterns
	 */
	public MappedInterceptor(String[] includePatterns, String[] excludePatterns, WebRequestInterceptor interceptor) {
		this(includePatterns, excludePatterns, new WebRequestHandlerInterceptorAdapter(interceptor));
	}


	/**
	 * Configure a PathMatcher to use with this MappedInterceptor instead of the one passed
	 * by default to the {@link #matches(String, org.springframework.util.PathMatcher)} method.
	 * <p>This is an advanced property that is only required when using custom PathMatcher
	 * implementations that support mapping metadata other than the Ant-style path patterns
	 * supported by default.
	 */
	public void setPathMatcher(PathMatcher pathMatcher) {
		this.pathMatcher = pathMatcher;
	}

	/**
	 * The configured PathMatcher, or {@code null} if none.
	 */
	public PathMatcher getPathMatcher() {
		return this.pathMatcher;
	}

	/**
	 * The path into the application the interceptor is mapped to.
	 */
	public String[] getPathPatterns() {
		return this.includePatterns;
	}

	/**
	 * The actual {@link HandlerInterceptor} reference.
	 */
	public HandlerInterceptor getInterceptor() {
		return this.interceptor;
	}


	/**
	 * Determine a match for the given lookup path.
	 * @param lookupPath the current request path
	 * @param pathMatcher a path matcher for path pattern matching
	 */
	public boolean matches(String lookupPath, PathMatcher pathMatcher) {
		PathMatcher pathMatcherToUse = (this.pathMatcher != null ? this.pathMatcher : pathMatcher);
		if (!ObjectUtils.isEmpty(this.excludePatterns)) {
			for (String pattern : this.excludePatterns) {
				if (pathMatcherToUse.match(pattern, lookupPath)) {
					return false;
				}
			}
		}
		if (ObjectUtils.isEmpty(this.includePatterns)) {
			return true;
		}
		for (String pattern : this.includePatterns) {
			if (pathMatcherToUse.match(pattern, lookupPath)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		return this.interceptor.preHandle(request, response, handler);
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {

		this.interceptor.postHandle(request, response, handler, modelAndView);
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception ex) throws Exception {

		this.interceptor.afterCompletion(request, response, handler, ex);
	}

}
