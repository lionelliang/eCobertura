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
package ecobertura.core

import java.util.logging.Logger

import org.eclipse.core.runtime.Plugin
import org.osgi.framework.BundleContext

import log.EclipseLogger
import results.CoverageResultsCollector
import state.PluginState
import trace.Trace

object CorePlugin {
  private val internalPluginId = "ecobertura.core" //$NON-NLS-1$
  private val logger = Logger.getLogger(internalPluginId)
  	
  private var internalInstance: CorePlugin = null
  	
  def instance = internalInstance
  def pluginId = internalPluginId
}

class CorePlugin extends Plugin {
  import CorePlugin._
	
  private var internalPluginState: PluginState = null
  private var internalResultsCollector: CoverageResultsCollector = null
	
  def pluginState = internalPluginState
  def coverageResultsCollector = internalResultsCollector

  override def start(context: BundleContext): Unit = {
    if (internalInstance == null) {
      super.start(context)
      internalInstance = this
			
      Trace.configureForPluginId(pluginId)
      EclipseLogger.logFor(getLog)
      	
      internalPluginState = PluginState.initialize(getStateLocation)
      internalResultsCollector = CoverageResultsCollector.collect
      			
      logger.info("plugin started") //$NON-NLS-1$
    }
  }
	
  override def stop(context: BundleContext): Unit = {
    if (internalInstance != null) {
      internalPluginState.cleanUp
      internalInstance = null
      internalResultsCollector.stopCollecting
      super.stop(context)
      
      logger.info("plugin stopped") //$NON-NLS-1$
    }
  }
}
