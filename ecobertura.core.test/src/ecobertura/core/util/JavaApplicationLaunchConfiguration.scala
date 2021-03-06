/*
 * This file is part of eCobertura.
 * 
 * Copyright (c) 2009, 2010 Joachim Hofer
 * All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package ecobertura.core.util

import scala.collection.JavaConversions._
import scala.collection.mutable

import org.eclipse.core.runtime.CoreException
import org.eclipse.debug.core._
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants

object JavaApplicationLaunchConfiguration {
	def forProject(javaProject: JavaProject) = new JavaApplicationLaunchConfiguration(javaProject)
}

class JavaApplicationLaunchConfiguration(javaProject: JavaProject) {
	def createForMainType(mainTypeName: String) = {
		val configWC = retrieveJavaApplicationConfigurationWorkingCopy
		configWC setAttribute (IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, mainTypeName)
		updateClasspath(configWC)
		configWC.doSave
	}
	
	private def retrieveJavaApplicationConfigurationWorkingCopy = {
		val launchMgr = DebugPlugin.getDefault.getLaunchManager
		val javaLaunchType = launchMgr getLaunchConfigurationType 
				IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION
		val configWC = javaLaunchType newInstance (null, "sampleLaunchConfig")
		configWC
	}
	
	// FIXME this doesn't work for some reason...
	private def updateClasspath(configWC: ILaunchConfigurationWorkingCopy) = {
		var classpath = configWC.getAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, 
				mutable.Buffer[Any]()).asInstanceOf[mutable.Buffer[String]]
		classpath add javaProject.defaultClasspath.getMemento
		configWC setAttribute (IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false)
		configWC setAttribute (IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, classpath)
	}
}
