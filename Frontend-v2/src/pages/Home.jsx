import { useState } from "react";
import UploadZone from "../components/upload/UploadZone";
import ReportView from "../components/report/ReportView";
import logo from "../assets/logo.png"

function Home() {
  const [report, setReport] = useState(null);

  const handleReset = () => {
    setReport(null);
  };

  return (
    <main className="min-h-screen bg-[#07111f] text-white">
      <div className="mx-auto flex min-h-screen max-w-7xl flex-col px-6 py-8">
        {/* Header */}

        <header className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <img
              src={logo}
              alt="MedNemesis logo"
              className="h-16 object-contain"
            />

            <div>
              <h1 className="text-2xl font-bold tracking-tight">MedNemesis</h1>

              <p className="mt-1 text-sm text-slate-400">
                Medical Report Explanation
              </p>
            </div>
          </div>
        </header>

        {/* Main */}

        <section className="flex flex-1 flex-col items-center py-0">
          {!report && (
            <>
              {/* Hero */}

              <div className="mb-10 max-w-2xl text-center">
                <div className="mb-4 inline-flex rounded-full border border-cyan-400/20 bg-cyan-400/5 px-4 py-2 text-xs font-medium text-cyan-300">
                  AI-powered report explanation
                </div>

                <h2 className="text-4xl font-bold tracking-tight sm:text-5xl">
                  Understand your
                  <span className="block text-cyan-300">medical reports.</span>
                </h2>

                <p className="mx-auto mt-5 max-w-xl text-base leading-7 text-slate-400">
                  Upload your medical report and MedNemesis will extract the
                  information and explain the findings in simple, understandable
                  language.
                </p>
              </div>

              {/* Upload */}

              <UploadZone onAnalysisComplete={setReport} />
            </>
          )}

          {/* Report */}

          {report && <ReportView report={report} onReset={handleReset} />}
        </section>

        {/* Footer */}

        <footer className="py-6 text-center text-xs text-slate-600">
          MedNemesis provides educational explanations and does not replace
          professional medical advice.
        </footer>
      </div>
    </main>
  );
}

export default Home;
