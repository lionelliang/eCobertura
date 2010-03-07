package ecobertura.ui.annotation

import scala.collection.JavaConversions

import java.util.logging._

import org.eclipse.jface.text._
import org.eclipse.jface.text.source._
import org.eclipse.ui.texteditor.ITextEditor

import ecobertura.core.data.LineCoverage
import ecobertura.ui.editors.LineCoverageFinder
import ecobertura.ui.views.session.CoverageSessionModel

// TODO fire events to listeners
// TODO react to document/editor changes 
// TODO react to coverage changes
object CoverageAnnotationModel {
	private val logger = Logger.getLogger("ecobertura.ui.annotation") //$NON-NLS-1$

	class Key { /* internal marker class */ }
	val MODEL_ID = new Key
	
	def attachTo(editor: ITextEditor) =
		AnnotationModelAttacher.attachTo(editor)
	
	def createForEditorDocument(editor: ITextEditor, document: IDocument) = {
		new CoverageAnnotationModel(editor, document)
	}
}

class CoverageAnnotationModel(editor: ITextEditor, document: IDocument) 
		extends AbstractAnnotationModel {
	import CoverageAnnotationModel.logger
	
	private var annotations = List[CoverageAnnotation]()
	
	logger.fine("CoverageAnnotationModel created.") //$NON-NLS-1$
	initializeAnnotations(editor, document)
	
	private def initializeAnnotations(editor: ITextEditor, document: IDocument) = {
		
		CoverageSessionModel.get.coverageSession match {
			case Some(session) => {
				logger.fine("CoverageSession active") /* session active */
				val coveredLines = LineCoverageFinder.forSession(session).findInEditor(editor)
				annotateLines(coveredLines)
			}
			case _ => /* nothing to do */
		}

		def annotateLines(lines: List[LineCoverage]) = {
			for (line <- lines; lineNumber = line.lineNumber - 1) {
				if (document.getLineLength(lineNumber) > 0) {
					val annotation = CoverageAnnotation.fromPosition(document.getLineOffset(lineNumber), 
							document.getLineLength(lineNumber))
					annotations ::= annotation
					val event = new AnnotationModelEvent(this)
					event.annotationAdded(annotation)
					fireModelChanged(event)
				}
			}
		}
	}
	
	override def connect(document: IDocument) = {
		logger.fine("CoverageAnnotationModel connected") //$NON-NLS-1$
		addAnnotationsTo(document)
		
		def addAnnotationsTo(document: IDocument) = {
			try {
				annotations.foreach(annotation => document.addPosition(annotation.getPosition))
			} catch {
				case e: BadLocationException =>
					logger.log(Level.WARNING, "unable to add annotation to document", e) //$NON-NLS-1$
			}
		}
	}
	
	override def disconnect(document: IDocument) = {
		removeAnnotationsFrom(document)
		logger.fine("CoverageAnnotationModel disconnected") //$NON-NLS-1$
		
		def removeAnnotationsFrom(document: IDocument) =
			annotations.foreach(annotation => document.removePosition(annotation.getPosition))
	}
	
	override def getAnnotationIterator = JavaConversions.asIterator(annotations.iterator)
	
	override def getPosition(annotation: Annotation) = annotation match {
		case coverageAnnotation: CoverageAnnotation => coverageAnnotation.getPosition
		case _ => null
	}
}
