package com.huang.security;

import cn.hutool.core.util.StrUtil;
import com.huang.entity.User;
import com.huang.service.UserService;
import com.huang.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;


import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

//JWT认证过滤器
public class JwtAuthenticationFilter extends BasicAuthenticationFilter {

	@Autowired
	JwtUtils jwtUtils;

	@Autowired
	UserDetailServiceImpl userDetailService;

	@Autowired
	UserService userService;

	public JwtAuthenticationFilter(AuthenticationManager authenticationManager) {
		super(authenticationManager);
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

		String jwt = request.getHeader(jwtUtils.getHeader());
		if (StrUtil.isBlankOrUndefined(jwt)) {
			chain.doFilter(request, response);
			return;
		}

		//解析JWt
		Claims claim = jwtUtils.getClaimByToken(jwt);
		if (claim == null) {
			throw new JwtException("token 异常");
		}
		if (jwtUtils.isTokenExpired(claim)) {
			throw new JwtException("token已过期");
		}

		String username = claim.getSubject();
		// 获取用户的权限等信息

		User user = userService.getByName(username);

		UsernamePasswordAuthenticationToken token
				= new UsernamePasswordAuthenticationToken(username, null, userDetailService.getUserAuthority(user.getUserId()));

		//System.out.println("权限为"+userDetailService.getUserAuthority(user.getUserId()));

		SecurityContextHolder.getContext().setAuthentication(token);

		chain.doFilter(request, response);
	}
}
