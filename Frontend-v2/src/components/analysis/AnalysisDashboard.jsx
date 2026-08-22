import {
  AlertTriangle,
  CheckCircle2,
  FileText,
  HeartPulse,
  Info,
  Download,
  ShieldCheck,
  ChevronDown,
  ChevronUp,
} from "lucide-react";
import { useState } from "react";

function AnalysisDashboard({ report }) {
  if (!report) {
    return null;
  }

  const [showOCR, setShowOCR] = useState(false);

  const analysis = report.analysis || "";
  const sections = parseAnalysis(analysis);

  return (
    <div className="w-full space-y-6">
      {/* =====================================================
          REPORT HEADER
      ====================================================== */}

      <div className="rounded-3xl border border-slate-800 bg-slate-900/70 p-6 sm:p-8">
        <div className="flex flex-col gap-6 lg:flex-row lg:items-center lg:justify-between">
          <div className="flex items-start gap-4">
            <div className="flex h-14 w-14 shrink-0 items-center justify-center rounded-2xl bg-cyan-400/10 text-cyan-300">
              <FileText size={26} />
            </div>

            <div>
              <p className="text-xs font-semibold uppercase tracking-[0.2em] text-cyan-400">
                Analysis Complete
              </p>

              <h2 className="mt-2 text-2xl font-bold text-white sm:text-3xl">
                Medical Report Analysis
              </h2>

              <p className="mt-2 max-w-xl text-sm leading-6 text-slate-400">
                AI-generated educational explanation of the uploaded medical
                report.
              </p>
            </div>
          </div>

          {report.pdfUrl && (
            <a
              href={getPdfUrl(report.pdfUrl)}
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex shrink-0 items-center justify-center gap-2 rounded-xl bg-cyan-400 px-5 py-3 text-sm font-semibold text-slate-950 transition hover:bg-cyan-300"
            >
              <Download size={17} />
              Download PDF
            </a>
          )}
        </div>
      </div>

      {/* =====================================================
          QUICK OVERVIEW
      ====================================================== */}

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        <InfoCard
          icon={<FileText size={19} />}
          label="Report"
          value={report.filename || "Medical Report"}
        />

        <InfoCard
          icon={<HeartPulse size={19} />}
          label="Status"
          value="Successfully analyzed"
          valueClass="text-emerald-400"
        />

        <InfoCard
          icon={<Info size={19} />}
          label="Analysis"
          value="Educational explanation"
        />
      </div>

      {/* =====================================================
          FINDINGS
      ====================================================== */}

      <div className="grid gap-6 lg:grid-cols-2">
        <FindingCard
          title="Abnormal Findings"
          icon={<AlertTriangle size={20} />}
          iconClass="text-amber-300 bg-amber-400/10"
          content={sections.abnormal}
          emptyText="No abnormal findings were identified in the analysis."
        />

        <FindingCard
          title="Normal Results"
          icon={<CheckCircle2 size={20} />}
          iconClass="text-emerald-300 bg-emerald-400/10"
          content={sections.normal}
          emptyText="No normal-results section was returned."
        />
      </div>

      {/* =====================================================
          FINDINGS SUMMARY
      ====================================================== */}

      {sections.summary && (
        <section className="rounded-3xl border border-cyan-400/10 bg-cyan-400/5 p-6 sm:p-8">
          <div className="mb-5 flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-cyan-400/10 text-cyan-300">
              <HeartPulse size={20} />
            </div>

            <div>
              <p className="text-xs font-semibold uppercase tracking-[0.18em] text-cyan-400">
                Findings
              </p>

              <h3 className="mt-1 text-xl font-bold text-white">Summary</h3>
            </div>
          </div>

          <MarkdownContent content={sections.summary} />
        </section>
      )}

      {/* =====================================================
          DIAGNOSIS + SEVERITY
      ====================================================== */}

      <div className="grid gap-6 lg:grid-cols-2">
        {sections.diagnosis && (
          <section className="rounded-3xl border border-slate-800 bg-slate-900/70 p-6">
            <SectionTitle title="Diagnosis" />

            <MarkdownContent content={sections.diagnosis} />
          </section>
        )}

        {sections.severity && (
          <section className="rounded-3xl border border-slate-800 bg-slate-900/70 p-6">
            <SectionTitle title="Severity Assessment" />

            <MarkdownContent content={sections.severity} />
          </section>
        )}
      </div>

      {/* =====================================================
          FOLLOW-UP
      ====================================================== */}

      {sections.followUp && (
        <section className="rounded-3xl border border-slate-800 bg-slate-900/70 p-6 sm:p-8">
          <SectionTitle title="Suggested Follow-Up" />

          <MarkdownContent content={sections.followUp} />
        </section>
      )}

      {/* =====================================================
          TREATMENT
      ====================================================== */}

      {sections.treatment && (
        <section className="rounded-3xl border border-slate-800 bg-slate-900/70 p-6 sm:p-8">
          <SectionTitle title="Treatment & General Recommendations" />

          <MarkdownContent content={sections.treatment} />
        </section>
      )}

      {/* =====================================================
          PROGNOSIS
      ====================================================== */}

      {sections.prognosis && (
        <section className="rounded-3xl border border-slate-800 bg-slate-900/70 p-6 sm:p-8">
          <div className="mb-5 flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-cyan-400/10 text-cyan-300">
              <HeartPulse size={20} />
            </div>

            <div>
              <p className="text-xs font-semibold uppercase tracking-[0.18em] text-cyan-400">
                Outlook
              </p>

              <h3 className="mt-1 text-xl font-bold text-white">Prognosis</h3>
            </div>
          </div>

          <MarkdownContent content={sections.prognosis} />
        </section>
      )}

      {/* =====================================================
          PREVENTIVE CARE
      ====================================================== */}

      {sections.preventiveCare && (
        <section className="rounded-3xl border border-emerald-400/10 bg-emerald-400/5 p-6 sm:p-8">
          <div className="mb-5 flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-emerald-400/10 text-emerald-300">
              <ShieldCheck size={20} />
            </div>

            <div>
              <p className="text-xs font-semibold uppercase tracking-[0.18em] text-emerald-400">
                Prevention
              </p>

              <h3 className="mt-1 text-xl font-bold text-white">
                Preventive Care Recommendations
              </h3>
            </div>
          </div>

          <MarkdownContent content={sections.preventiveCare} />
        </section>
      )}

      {/* =====================================================
          CONCLUSION
      ====================================================== */}

      {sections.conclusion && (
        <section className="rounded-3xl border border-slate-800 bg-slate-900/70 p-6 sm:p-8">
          <SectionTitle title="Conclusion" />

          <MarkdownContent content={sections.conclusion} />
        </section>
      )}

      {/* =====================================================
    RAW OCR TEXT
====================================================== */}

      {report.ocrText && (
        <div className="overflow-hidden rounded-2xl border border-slate-800 bg-slate-900/60">
          <button
            type="button"
            onClick={() => setShowOCR((value) => !value)}
            className="flex w-full items-center justify-between px-5 py-4 text-left transition hover:bg-slate-900"
          >
            <div>
              <p className="font-medium text-white">Extracted OCR Text</p>

              <p className="mt-1 text-xs text-slate-500">
                View the raw text extracted from your uploaded report.
              </p>
            </div>

            {showOCR ? (
              <ChevronUp size={18} className="text-slate-500" />
            ) : (
              <ChevronDown size={18} className="text-slate-500" />
            )}
          </button>

          {showOCR && (
            <pre className="max-h-125 overflow-auto whitespace-pre-wrap border-t border-slate-800 bg-slate-950 p-5 text-xs leading-6 text-slate-400">
              {report.ocrText}
            </pre>
          )}
        </div>
      )}

      {/* =====================================================
          DISCLAIMER
      ====================================================== */}

      <div className="rounded-2xl border border-amber-400/10 bg-amber-400/5 p-5">
        <div className="flex gap-3">
          <Info size={18} className="mt-0.5 shrink-0 text-amber-300" />

          <p className="text-xs leading-6 text-slate-400">
            MedNemesis provides educational explanations of medical reports. It
            does not provide a medical diagnosis and does not replace advice
            from a qualified healthcare professional.
          </p>
        </div>
      </div>
    </div>
  );
}

/* =========================================================
   INFO CARD
========================================================= */

function InfoCard({ icon, label, value, valueClass = "text-white" }) {
  return (
    <div className="rounded-2xl border border-slate-800 bg-slate-900/70 p-5">
      <div className="flex items-center gap-3">
        <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-slate-800 text-slate-300">
          {icon}
        </div>

        <p className="text-xs font-semibold uppercase tracking-wider text-slate-500">
          {label}
        </p>
      </div>

      <p className={`mt-4 truncate text-sm font-semibold ${valueClass}`}>
        {value}
      </p>
    </div>
  );
}

/* =========================================================
   FINDING CARD
========================================================= */

function FindingCard({ title, icon, iconClass, content, emptyText }) {
  return (
    <section className="rounded-3xl border border-slate-800 bg-slate-900/70 p-6">
      <div className="mb-5 flex items-center gap-3">
        <div
          className={`flex h-10 w-10 items-center justify-center rounded-xl ${iconClass}`}
        >
          {icon}
        </div>

        <h3 className="text-xl font-bold text-white">{title}</h3>
      </div>

      {content ? (
        <MarkdownContent content={content} />
      ) : (
        <p className="text-sm leading-6 text-slate-500">{emptyText}</p>
      )}
    </section>
  );
}

/* =========================================================
   SECTION TITLE
========================================================= */

function SectionTitle({ title }) {
  return (
    <div className="mb-5 border-b border-slate-800 pb-4">
      <h3 className="text-xl font-bold text-white">{title}</h3>
    </div>
  );
}

/* =========================================================
   MARKDOWN CONTENT
========================================================= */

function MarkdownContent({ content }) {
  if (!content) {
    return null;
  }

  const lines = content.split(/\r?\n/);

  const elements = [];

  let listItems = [];

  const flushList = () => {
    if (!listItems.length) {
      return;
    }

    elements.push(
      <ul key={`list-${elements.length}`} className="my-4 space-y-2 pl-5">
        {listItems.map((item, index) => (
          <li
            key={index}
            className="list-disc text-sm leading-7 text-slate-300"
          >
            <InlineMarkdown text={item} />
          </li>
        ))}
      </ul>,
    );

    listItems = [];
  };

  lines.forEach((rawLine, index) => {
    const line = rawLine.trim();

    if (!line) {
      flushList();
      return;
    }

    /* Markdown separator */

    if (/^\\?---+$/.test(line)) {
      flushList();

      elements.push(
        <div key={`separator-${index}`} className="my-5 h-px bg-slate-800" />,
      );

      return;
    }

    /* Bullet list */

    const bullet = line.match(/^[-*+]\s+(.+)$/);

    if (bullet) {
      listItems.push(bullet[1]);
      return;
    }

    /* Numbered list */

    const numbered = line.match(/^\d+[.)]\s+(.+)$/);

    if (numbered) {
      flushList();

      const number = line.match(/^\d+/)?.[0] || "";

      elements.push(
        <div
          key={`number-${index}`}
          className="my-2 flex gap-3 text-sm leading-7 text-slate-300"
        >
          <span className="font-semibold text-cyan-400">{number}.</span>

          <span>
            <InlineMarkdown text={numbered[1]} />
          </span>
        </div>,
      );

      return;
    }

    /* Normal paragraph */

    flushList();

    elements.push(
      <p
        key={`paragraph-${index}`}
        className="my-3 text-sm leading-7 text-slate-300"
      >
        <InlineMarkdown text={line} />
      </p>,
    );
  });

  flushList();

  return <div>{elements}</div>;
}

/* =========================================================
   INLINE MARKDOWN
========================================================= */

function InlineMarkdown({ text }) {
  if (!text) {
    return null;
  }

  const parts = text.split(/(\*\*[^*]+\*\*|\*[^*]+\*)/g);

  return parts.map((part, index) => {
    if (part.startsWith("**") && part.endsWith("**")) {
      return (
        <strong key={index} className="font-semibold text-white">
          {part.slice(2, -2)}
        </strong>
      );
    }

    if (part.startsWith("*") && part.endsWith("*")) {
      return (
        <em key={index} className="text-slate-300">
          {part.slice(1, -1)}
        </em>
      );
    }

    return <span key={index}>{part}</span>;
  });
}

/* =========================================================
   PARSE GEMINI SECTIONS
========================================================= */

function parseAnalysis(markdown) {
  const sections = {
    summary: "",
    normal: "",
    abnormal: "",
    diagnosis: "",
    severity: "",
    followUp: "",
    treatment: "",
    prognosis: "",
    preventiveCare: "",
    conclusion: "",
  };

  if (!markdown || !markdown.trim()) {
    return sections;
  }

  const headingRegex = /^#{1,6}\s*(\d+)\.\s*(.+?)\s*$/gm;

  const matches = [...markdown.matchAll(headingRegex)];

  for (let i = 0; i < matches.length; i++) {
    const match = matches[i];

    const sectionNumber = Number(match[1]);

    const title = match[2].trim().toLowerCase().replace(/:$/, "").trim();

    const contentStart = match.index + match[0].length;

    const contentEnd =
      i + 1 < matches.length ? matches[i + 1].index : markdown.length;

    let content = markdown.slice(contentStart, contentEnd).trim();

    /*
     * Remove markdown separators.
     */

    content = content.replace(/^\\?---+\s*$/gm, "").trim();

    /*
     * =====================================================
     * SECTION NUMBER
     * =====================================================
     */

    switch (sectionNumber) {
      case 3:
        sections.summary = content;
        break;

      case 4:
        sections.normal = content;
        break;

      case 5:
        sections.abnormal = content;
        break;

      case 6:
        sections.diagnosis = content;
        break;

      case 7:
        sections.severity = content;
        break;

      case 8:
        sections.followUp = content;
        break;

      case 9:
        sections.treatment = content;
        break;

      case 10:
        sections.prognosis = content;
        break;

      case 11:
        sections.preventiveCare = content;
        break;

      case 12:
        sections.conclusion = content;
        break;

      default:
        break;
    }

    if (title === "findings summary") {
      sections.summary = content;
    }

    if (title === "normal results") {
      sections.normal = content;
    }

    if (title === "abnormal results") {
      sections.abnormal = content;
    }

    if (title === "diagnosis") {
      sections.diagnosis = content;
    }

    if (title === "severity assessment") {
      sections.severity = content;
    }

    if (title === "suggested follow-up") {
      sections.followUp = content;
    }

    if (
      title === "treatment recommendations" ||
      title === "treatment & general recommendations"
    ) {
      sections.treatment = content;
    }

    if (title === "prognosis") {
      sections.prognosis = content;
    }

    if (title === "preventive care recommendations") {
      sections.preventiveCare = content;
    }

    if (title === "conclusion") {
      sections.conclusion = content;
    }
  }

  return sections;
}

/* =========================================================
   PDF URL
========================================================= */

function getPdfUrl(pdfUrl) {
  if (pdfUrl.startsWith("http")) {
    return pdfUrl;
  }

  return `http://localhost:8080${pdfUrl}`;
}

export default AnalysisDashboard;
