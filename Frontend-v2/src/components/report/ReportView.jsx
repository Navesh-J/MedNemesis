import { FileText, Download, RotateCcw, ChevronDown } from "lucide-react";

import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";

function ReportView({ report, onReset }) {
  if (!report) {
    return null;
  }

  return (
    <section className="w-full max-w-5xl">
      {/* =====================================================
          REPORT HEADER
      ====================================================== */}

      <div className="overflow-hidden rounded-3xl border border-slate-800 bg-slate-900/70 shadow-xl shadow-black/10">
        <div className="p-6 sm:p-7">
          <div className="flex flex-col gap-6 lg:flex-row lg:items-center lg:justify-between">
            <div className="flex min-w-0 items-center gap-4">
              <div className="flex h-13 w-13 shrink-0 items-center justify-center rounded-2xl border border-cyan-400/10 bg-cyan-400/10 text-cyan-300">
                <FileText size={25} />
              </div>

              <div className="min-w-0">
                <div className="flex items-center gap-2">
                  <span className="h-2 w-2 rounded-full bg-emerald-400" />

                  <p className="text-xs font-medium uppercase tracking-[0.15em] text-emerald-400">
                    Analysis Complete
                  </p>
                </div>

                <h2 className="mt-1 truncate text-lg font-semibold text-white sm:text-xl">
                  {report.filename}
                </h2>
              </div>
            </div>

            <div className="flex flex-wrap gap-3">
              {report.pdfUrl && (
                <a
                  href={report.pdfUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="inline-flex items-center justify-center gap-2 rounded-xl bg-cyan-400 px-4 py-2.5 text-sm font-semibold text-slate-950 transition hover:bg-cyan-300 hover:shadow-lg hover:shadow-cyan-400/10"
                >
                  <Download size={17} />
                  Download PDF
                </a>
              )}

              <button
                type="button"
                onClick={onReset}
                className="inline-flex items-center justify-center gap-2 rounded-xl border border-slate-700 bg-slate-800 px-4 py-2.5 text-sm font-medium text-slate-300 transition hover:border-slate-600 hover:bg-slate-700 hover:text-white"
              >
                <RotateCcw size={17} />
                New Report
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* =====================================================
          AI ANALYSIS
      ====================================================== */}

      <div className="mt-6 overflow-hidden rounded-3xl border border-slate-800 bg-slate-900/70 shadow-xl shadow-black/10">
        {/* Analysis header */}

        <div className="border-b border-slate-800 bg-slate-900/80 px-6 py-5 sm:px-8">
          <p className="text-xs font-semibold uppercase tracking-[0.15em] text-cyan-400">
            AI Explanation
          </p>

          <h3 className="mt-1 text-xl font-semibold text-white">
            Medical Report Analysis
          </h3>

          <p className="mt-1 text-sm text-slate-500">
            An easy-to-understand explanation generated from the extracted
            report text.
          </p>
        </div>

        {/* Markdown */}

        <article className="px-6 py-7 sm:px-8 sm:py-9">
          <ReactMarkdown
            remarkPlugins={[remarkGfm]}
            components={{
              /* =================================================
                 HEADINGS
              ================================================== */

              h1: ({ children }) => (
                <div className="mb-6 mt-10 first:mt-0">
                  <h1 className="border-b border-slate-800 pb-3 text-2xl font-bold tracking-tight text-white sm:text-3xl">
                    {children}
                  </h1>
                </div>
              ),

              h2: ({ children }) => (
                <h2 className="mb-4 mt-9 text-xl font-bold tracking-tight text-cyan-300 sm:text-2xl">
                  {children}
                </h2>
              ),

              h3: ({ children }) => (
                <h3 className="mb-3 mt-7 text-lg font-semibold text-white">
                  {children}
                </h3>
              ),

              h4: ({ children }) => (
                <h4 className="mb-2 mt-5 font-semibold text-slate-200">
                  {children}
                </h4>
              ),

              /* =================================================
                 PARAGRAPHS
              ================================================== */

              p: ({ children }) => (
                <p className="mb-5 max-w-4xl text-sm leading-7 text-slate-300 sm:text-[15px]">
                  {children}
                </p>
              ),

              /* =================================================
                 BOLD / ITALIC
              ================================================== */

              strong: ({ children }) => (
                <strong className="font-semibold text-white">{children}</strong>
              ),

              em: ({ children }) => (
                <em className="text-slate-400">{children}</em>
              ),

              /* =================================================
                 UNORDERED LIST
              ================================================== */

              ul: ({ children }) => (
                <ul className="mb-5 ml-5 space-y-2.5 text-sm leading-7 text-slate-300 marker:text-cyan-400 sm:text-[15px]">
                  {children}
                </ul>
              ),

              /* =================================================
                 ORDERED LIST
              ================================================== */

              ol: ({ children }) => (
                <ol className="mb-5 ml-6 space-y-2.5 text-sm leading-7 text-slate-300 marker:font-semibold marker:text-cyan-400 sm:text-[15px]">
                  {children}
                </ol>
              ),

              /* =================================================
                 LIST ITEM
              ================================================== */

              li: ({ children }) => <li className="pl-1">{children}</li>,

              /* =================================================
                 HORIZONTAL RULE
              ================================================== */

              hr: () => <div className="my-8 h-px bg-slate-800" />,

              /* =================================================
                 BLOCKQUOTE
              ================================================== */

              blockquote: ({ children }) => (
                <blockquote className="my-6 rounded-r-xl border-l-4 border-cyan-400/40 bg-slate-950/50 px-5 py-4 text-sm leading-7 text-slate-400">
                  {children}
                </blockquote>
              ),

              /* =================================================
                 LINKS
              ================================================== */

              a: ({ href, children }) => (
                <a
                  href={href}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="font-medium text-cyan-400 underline decoration-cyan-400/30 underline-offset-4 transition hover:text-cyan-300"
                >
                  {children}
                </a>
              ),

              /* =================================================
                 INLINE CODE
              ================================================== */

              code: ({ children }) => (
                <code className="rounded-md bg-slate-950 px-1.5 py-0.5 font-mono text-xs text-cyan-300">
                  {children}
                </code>
              ),

              /* =================================================
                 CODE BLOCK
              ================================================== */

              pre: ({ children }) => (
                <pre className="my-5 overflow-x-auto rounded-2xl border border-slate-800 bg-slate-950 p-5 text-sm">
                  {children}
                </pre>
              ),

              /* =================================================
                 TABLE
              ================================================== */

              table: ({ children }) => (
                <div className="my-6 overflow-x-auto rounded-2xl border border-slate-800">
                  <table className="w-full min-w-150 text-left text-sm">
                    {children}
                  </table>
                </div>
              ),

              thead: ({ children }) => (
                <thead className="bg-slate-950 text-slate-200">
                  {children}
                </thead>
              ),

              tbody: ({ children }) => (
                <tbody className="divide-y divide-slate-800">{children}</tbody>
              ),

              th: ({ children }) => (
                <th className="px-4 py-3 font-semibold">{children}</th>
              ),

              td: ({ children }) => (
                <td className="px-4 py-3 text-slate-300">{children}</td>
              ),
            }}
          >
            {report.analysis}
          </ReactMarkdown>
        </article>
      </div>

      {/* =====================================================
          OCR TEXT
      ====================================================== */}

      <details className="group mt-6 overflow-hidden rounded-3xl border border-slate-800 bg-slate-900/50">
        <summary className="flex cursor-pointer list-none items-center justify-between px-6 py-5 transition hover:bg-slate-900/70 sm:px-7">
          <div>
            <p className="text-sm font-semibold text-slate-300">
              Extracted OCR Text
            </p>

            <p className="mt-1 text-xs text-slate-600">
              View the raw text extracted from your uploaded report
            </p>
          </div>

          <ChevronDown
            size={19}
            className="text-slate-500 transition-transform duration-300 group-open:rotate-180"
          />
        </summary>

        <div className="border-t border-slate-800 px-6 py-6 sm:px-7">
          <pre className="max-h-96 overflow-auto whitespace-pre-wrap wrap-break-word rounded-2xl border border-slate-800 bg-slate-950 p-5 font-mono text-xs leading-6 text-slate-500">
            {report.ocrText}
          </pre>
        </div>
      </details>

      {/* =====================================================
          DISCLAIMER
      ====================================================== */}

      <div className="mt-6 rounded-2xl border border-amber-500/10 bg-amber-500/5 px-5 py-4">
        <p className="text-xs leading-5 text-amber-200/60">
          <span className="font-semibold text-amber-200/80">
            Educational use only.
          </span>{" "}
          MedNemesis explains medical reports in simpler language but does not
          provide a medical diagnosis or replace advice from a qualified
          healthcare professional.
        </p>
      </div>
    </section>
  );
}

export default ReportView;
