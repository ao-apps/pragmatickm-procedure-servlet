/*
 * pragmatickm-procedure-servlet - Procedures nested within SemanticCMS pages and elements in a Servlet environment.
 * Copyright (C) 2014, 2015, 2016, 2017, 2018, 2020, 2021  AO Industries, Inc.
 *     support@aoindustries.com
 *     7262 Bull Pen Cir
 *     Mobile, AL 36695
 *
 * This file is part of pragmatickm-procedure-servlet.
 *
 * pragmatickm-procedure-servlet is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * pragmatickm-procedure-servlet is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with pragmatickm-procedure-servlet.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.pragmatickm.procedure.servlet.impl;

import com.aoindustries.html.PalpableContent;
import com.aoindustries.io.buffer.BufferResult;
import com.pragmatickm.procedure.model.Procedure;
import com.semanticcms.core.model.ElementContext;
import com.semanticcms.core.model.NodeBodyWriter;
import com.semanticcms.core.servlet.PageIndex;
import java.io.IOException;

final public class ProcedureImpl {

	public static <__ extends PalpableContent<__>> void writeProcedureTable(
		PageIndex pageIndex,
		__ content,
		ElementContext context,
		Object style,
		Procedure procedure
	) throws IOException {
		content.table()
			.id(idAttr -> PageIndex.appendIdInPage(
				pageIndex,
				procedure.getPage(),
				procedure.getId(),
				idAttr
			))
			.clazz("ao-grid", "pragmatickm-procedure")
			.style(style)
		.__(table -> table
			.thead__(thead -> thead
				.tr__(tr -> tr
					.th__(th -> th
						.div__(procedure)
					)
				)
			)
			.tbody__(tbody -> {
				BufferResult body = procedure.getBody();
				if(body.getLength() > 0) {
					tbody.tr__(tr -> tr
						.td__(td ->
							body.writeTo(new NodeBodyWriter(procedure, td.getDocument().out, context))
						)
					);
				}
			})
		);
	}

	/**
	 * Make no instances.
	 */
	private ProcedureImpl() {
	}
}
