/**
 *  Uploadr, a multi-file uploader plugin
 *  Copyright (C) 2011 Jeroen Wesbeek
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  $Author$
 *  $Rev$
 *  $Date$
 */

package hungry.wombat

class UploadrTagLib {
	// define namespace
	static namespace = "uploadr"

	/**
	 * render an file uploadr element
	 * @param Map           attributes
	 * @param Closure       body
	 */
	def add = { attrs, body ->
		def sound 			= (attrs.get('noSound') && attrs.noSound) ? false : true;
		def name			= (attrs.name) ? attrs.name : "uploadr"
		def classname		= (attrs.class) ? attrs.class : 'uploadr'
		def direction 		= (attrs.direction) ? attrs.direction : 'down'
		def uri 			= createLink(controller: attrs.controller, action: attrs.action)
		def placeholder		= (attrs.get('placeholder') ? attrs.get('placeholder') : '')
		def fileselect		= (attrs.get('fileselect') ? attrs.get('fileselect') : '')
		def maxVisible		= (attrs.get('maxVisible') ? attrs.get('maxVisible') : 0);

		// define uri
		if (attrs.get('controller')) {
		    // got an action attribute?
			if (attrs.get('action')) {
				// got a plugin attribute?
				if (attrs.get('plugin')) {
					uri = createLink(plugin: attrs.plugin, controller: attrs.controller, action: attrs.action)
				} else {
					uri = createLink(controller: attrs.controller, action: attrs.action)
				}
			} else {
				// got a plugin attribute?
				if (attrs.get('plugin')) {
					uri = createLink(plugin: attrs.plugin, controller: attrs.controller)
				} else {
					uri = createLink(controller: attrs.controller)
				}
			}
		} else {
			// use default controller for handeling file uploads
			uri = createLink(plugin: 'uploadr', controller: 'upload', action: 'handle')
		}

		// got a path attribute?
		if (attrs.get('path')) {
			// initialize session if necessary
			if (!session.uploadr) session.uploadr = [:]

			// and remember stuff in the session
			if (!session.uploadr[name]) {
				session.uploadr[name] = [
					uri		: uri,
					path	: attrs.path
				]
			} else if (session.uploadr[name].path != attrs.path) {
				println "uploadr: warning! Another uploadr with the same name (${name}) is already using another upload path (${attrs.path}). Make sure you are using unique names for your uploadr elements!"
			}
		}

		// init pageScope
		pageScope.name			= name
		pageScope.path			= session.uploadr[name].path
		pageScope.handlers		= [:]
		pageScope.files			= [:]
		pageScope.temp			= [:]

		// make sure body tags are handled
		body()

		// render file upload div
		out << "<div name=\"${name}\" class=\"${classname}\"></div>"

		// and render inline initialization javascript
		out << r.script([:], g.render(
			plugin	: 'uploadr',
			template:'/js/init',
			model	:[
				name		: name,
				uri			: uri,
				direction 	: direction,
				placeholder	: placeholder,
				fileselect 	: fileselect,
				classname	: classname,
				maxVisible	: maxVisible,
				sound 		: sound,
				handlers	: pageScope.handlers,
				files		: pageScope.files,
				unsupported	: (attrs.get('unsupported')) ? attrs.unsupported : createLink(plugin: 'uploadr', controller: 'upload', action: 'warning')
			]
		))
	}

	def demo = { attrs, body ->
		// pull in external resources
		out << r.external(type:"css", plugin:'uploadr', dir:'css', file:'demo.css')
		out << r.external(type:"css", plugin:'uploadr', dir:'css', file:'demopage.css')
		out << r.external(type:"css", plugin:'uploadr', dir:'css', file:'shThemeEclipse.css')
		out << r.external(type:"css", plugin:'uploadr', dir:'css', file:'shCore.css')
		out << r.external(type:"js", plugin:'uploadr', dir:'js', file:'shCore.js')
		out << r.external(type:"js", plugin:'uploadr', dir:'js', file:'shAutoloader.js')
		out << r.external(type:"js", plugin:'uploadr', dir:'js', file:'shBrushXml.js')

		// initialize the syntax highlighter
		out << r.script([:], "\$(document).ready(function() { SyntaxHighlighter.all(); });")

		out << g.render(plugin: 'uploadr', template:'/upload/demo')
	}

	def onStart = { attrs, body ->
		pageScope.handlers.onStart = body()
	}

	def onProgress = { attrs, body ->
		pageScope.handlers.onProgress = body()
	}

	def onSuccess = { attrs, body ->
		pageScope.handlers.onSuccess = body()
	}

	def onFailure = { attrs, body ->
		pageScope.handlers.onFailure = body()
	}

	def onAbort = { attrs, body ->
		pageScope.handlers.onAbort = body()
	}

	def onDelete = { attrs, body ->
		pageScope.handlers.onDelete = body()
	}

	def onDownload = { attrs, body ->
		pageScope.handlers.onDownload = body()
	}

	def onView = { attrs, body ->
		pageScope.handlers.onView = body()
	}

	def file = { attrs, body ->
		if (!attrs.get('name')) return

		// use child tags to insert file
		pageScope.temp = [
			size		: 0 as Long,
			modified 	: 0 as Long,
			id 			: "",
			info 		: []
		]

		// do we have child tags to override the regular handler?
		if (!(body().trim())) {
			// try to read file from path
			def file = new File(pageScope.path, attrs.name)

			if (file.exists()) {
				pageScope.files[ "${attrs.name}" ] = [
				    size 		: file.size(),
					modified 	: file.lastModified()
				]
			} else {
				println "ignoring predefined file '${file}' as it does not exist!"
			}
		} else {
			pageScope.files[ "${attrs.name}" ] = pageScope.temp
		}
	}

	def fileInfo = { attrs, body ->
		def count

		if (pageScope.temp.info) {
			count = pageScope.temp.info.size()
		} else {
			count = 0
			pageScope.temp.info = []
		}

		pageScope.temp.info[ count ] = body()
		out << "fileInfo"
	}

	def fileSize = { attrs, body ->
		pageScope.temp.size = body() as Long
		out << "fileSize"
	}

	def fileModified = { attrs, body ->
		pageScope.temp.modified = body() as Long
		out << "fileModified"
	}

	def fileId = { attrs, body ->
		pageScope.temp.id = body() as String
		out << "fileId"
	}
}
