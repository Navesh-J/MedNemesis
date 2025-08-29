import {PrismaClient} from '@prisma/client'

const prisma = new PrismaClient();

async function main(){
    const parameters = [
    {
      name: "Hemoglobin",
      unit: "g/dL",
      minValue: 13.5,
      maxValue: 17.5,
      description: "Protein in red blood cells that carries oxygen.",
      synonyms: ["Hb", "HGB"]
    },
    {
      name: "White Blood Cells",
      unit: "x10^9/L",
      minValue: 4.0,
      maxValue: 11.0,
      description: "Cells that fight infections.",
      synonyms: ["WBC"]
    },
    {
      name: "Platelets",
      unit: "x10^9/L",
      minValue: 150,
      maxValue: 450,
      description: "Help blood clotting.",
      synonyms: ["PLT"]
    },
    {
      name: "Blood Glucose (Fasting)",
      unit: "mg/dL",
      minValue: 70,
      maxValue: 100,
      description: "Fasting blood sugar level.",
      synonyms: ["FBS", "Fasting Sugar"]
    },
    {
      name: "Total Cholesterol",
      unit: "mg/dL",
      minValue: 125,
      maxValue: 200,
      description: "Overall cholesterol level.",
      synonyms: ["Cholesterol"]
    },
    {
      name: "Triglycerides",
      unit: "mg/dL",
      minValue: 0,
      maxValue: 150,
      description: "Type of fat found in blood.",
      synonyms: ["TG"]
    },
    {
      name: "HDL Cholesterol",
      unit: "mg/dL",
      minValue: 40,
      maxValue: 60,
      description: "Good cholesterol.",
      synonyms: ["HDL"]
    },
    {
      name: "LDL Cholesterol",
      unit: "mg/dL",
      minValue: 0,
      maxValue: 100,
      description: "Bad cholesterol.",
      synonyms: ["LDL"]
    },
    {
      name: "Serum Creatinine",
      unit: "mg/dL",
      minValue: 0.7,
      maxValue: 1.3,
      description: "Kidney function marker.",
      synonyms: ["Creatinine"]
    },
    {
      name: "Blood Urea",
      unit: "mg/dL",
      minValue: 7,
      maxValue: 20,
      description: "Waste product from protein metabolism.",
      synonyms: ["Urea"]
    },
    // Liver Function Tests
    {
      name: "Alanine Aminotransferase (ALT)",
      unit: "U/L",
      minValue: 7,
      maxValue: 56,
      description: "Enzyme found in the liver; elevated levels may indicate liver damage.",
      synonyms: ["SGPT"]
    },
    {
      name: "Aspartate Aminotransferase (AST)",
      unit: "U/L",
      minValue: 10,
      maxValue: 40,
      description: "Enzyme found in liver and muscle; elevated levels can indicate liver damage.",
      synonyms: ["SGOT"]
    },
    {
      name: "Alkaline Phosphatase (ALP)",
      unit: "U/L",
      minValue: 44,
      maxValue: 147,
      description: "Enzyme related to liver and bone health.",
      synonyms: []
    },
    {
      name: "Bilirubin",
      unit: "mg/dL",
      minValue: 0.1,
      maxValue: 1.2,
      description: "Waste product from the breakdown of red blood cells; elevated levels can indicate liver dysfunction.",
      synonyms: []
    },
    {
      name: "Albumin",
      unit: "g/dL",
      minValue: 3.5,
      maxValue: 5.0,
      description: "Protein made by the liver, low levels may indicate liver or kidney disease.",
      synonyms: []
    },
    {
      name: "Total Protein",
      unit: "g/dL",
      minValue: 6.0,
      maxValue: 8.0,
      description: "Total amount of protein in the blood, which can indicate nutritional status or liver/kidney disease.",
      synonyms: []
    },
    // Thyroid Function Tests
    {
      name: "Thyroid Stimulating Hormone (TSH)",
      unit: "µIU/mL",
      minValue: 0.4,
      maxValue: 4.0,
      description: "Stimulates thyroid hormone production; high levels may indicate hypothyroidism.",
      synonyms: []
    },
    {
      name: "T3 (Triiodothyronine)",
      unit: "ng/dL",
      minValue: 80,
      maxValue: 180,
      description: "Thyroid hormone that regulates metabolism; low levels can indicate hypothyroidism.",
      synonyms: []
    },
    {
      name: "T4 (Thyroxine)",
      unit: "µg/dL",
      minValue: 4.5,
      maxValue: 12.0,
      description: "Thyroid hormone critical for regulating metabolism; abnormal levels can indicate thyroid disorders.",
      synonyms: []
    },
    // Electrolytes
    {
      name: "Sodium (Na)",
      unit: "mmol/L",
      minValue: 135,
      maxValue: 145,
      description: "Vital for nerve and muscle function, and maintaining fluid balance.",
      synonyms: []
    },
    {
      name: "Potassium (K)",
      unit: "mmol/L",
      minValue: 3.5,
      maxValue: 5.0,
      description: "Helps in nerve function and muscle contraction.",
      synonyms: []
    },
    {
      name: "Calcium (Ca)",
      unit: "mg/dL",
      minValue: 8.5,
      maxValue: 10.2,
      description: "Important for bone health and muscle function.",
      synonyms: []
    },
    {
      name: "Chloride (Cl)",
      unit: "mmol/L",
      minValue: 96,
      maxValue: 106,
      description: "Helps maintain fluid balance and acid-base balance.",
      synonyms: []
    },
    {
      name: "Bicarbonate (HCO3)",
      unit: "mmol/L",
      minValue: 22,
      maxValue: 28,
      description: "Helps regulate the body's acid-base balance.",
      synonyms: []
    },
    // Vitamins and Minerals
    {
      name: "Vitamin D (25-Hydroxyvitamin D)",
      unit: "ng/mL",
      minValue: 20,
      maxValue: 50,
      description: "Important for bone health and immune function.",
      synonyms: []
    },
    {
      name: "Vitamin B12",
      unit: "pg/mL",
      minValue: 200,
      maxValue: 900,
      description: "Important for nerve function and red blood cell production.",
      synonyms: []
    },
    {
      name: "Folate (Vitamin B9)",
      unit: "ng/mL",
      minValue: 2.0,
      maxValue: 20.0,
      description: "Critical for cell division and DNA synthesis.",
      synonyms: []
    },
    {
      name: "Iron",
      unit: "µg/dL",
      minValue: 50,
      maxValue: 170,
      description: "Important for oxygen transport in the blood.",
      synonyms: []
    },
    {
      name: "Ferritin",
      unit: "ng/mL",
      minValue: 20,
      maxValue: 500,
      description: "Storage form of iron in the body.",
      synonyms: []
    },
    // Inflammation and Infection Markers
    {
      name: "C-Reactive Protein (CRP)",
      unit: "mg/L",
      minValue: 0,
      maxValue: 10,
      description: "Marker for inflammation, elevated in infections and chronic diseases.",
      synonyms: []
    },
    {
      name: "Erythrocyte Sedimentation Rate (ESR)",
      unit: "mm/hr",
      minValue: 0,
      maxValue: 20,
      description: "Measure of inflammation; elevated levels may indicate infection or autoimmune disease.",
      synonyms: []
    },
    // Coagulation and Clotting
    {
      name: "Prothrombin Time (PT)",
      unit: "seconds",
      minValue: 11.0,
      maxValue: 13.5,
      description: "Measures the time it takes for blood to clot.",
      synonyms: []
    },
    {
      name: "International Normalized Ratio (INR)",
      unit: "ratio",
      minValue: 0.8,
      maxValue: 1.2,
      description: "Standardized test of blood clotting time.",
      synonyms: []
    },
    {
      name: "Activated Partial Thromboplastin Time (aPTT)",
      unit: "seconds",
      minValue: 25,
      maxValue: 35,
      description: "Test for clotting disorders.",
      synonyms: []
    },
    // Urine Tests
    {
      name: "Urine Protein",
      unit: "mg/dL",
      minValue: 0,
      maxValue: 20,
      description: "Presence of protein in urine can indicate kidney disease.",
      synonyms: []
    },
    {
      name: "Urine Glucose",
      unit: "mg/dL",
      minValue: 0,
      maxValue: 0,
      description: "Absence of glucose in urine is normal; presence may indicate diabetes.",
      synonyms: []
    },
    {
      name: "Urine Ketones",
      unit: "mg/dL",
      minValue: 0,
      maxValue: 5,
      description: "Presence of ketones can indicate uncontrolled diabetes or starvation.",
      synonyms: []
    },
    // Additional Hormonal and Metabolic Tests
    {
      name: "Testosterone",
      unit: "ng/dL",
      minValue: 300,
      maxValue: 1000,
      description: "Male hormone, used to assess hypogonadism and other hormonal imbalances.",
      synonyms: []
    },
    {
      name: "Insulin",
      unit: "µU/mL",
      minValue: 2.6,
      maxValue: 24.9,
      description: "Hormone that regulates blood glucose.",
      synonyms: []
    },
    {
      name: "Apolipoprotein A (ApoA)",
      unit: "mg/dL",
      minValue: 120,
      maxValue: 160,
      description: "Part of HDL cholesterol; higher levels are protective against cardiovascular disease.",
      synonyms: []
    },
    {
      name: "Apolipoprotein B (ApoB)",
      unit: "mg/dL",
      minValue: 40,
      maxValue: 130,
      description: "Part of LDL cholesterol; high levels are a risk factor for cardiovascular disease.",
      synonyms: []
    },
    // Additional Tumor Markers
    {
      name: "Prostate-Specific Antigen (PSA)",
      unit: "ng/mL",
      minValue: 0,
      maxValue: 4.0,
      description: "Screening for prostate cancer.",
      synonyms: []
    },
    {
      name: "Cancer Antigen 125 (CA-125)",
      unit: "U/mL",
      minValue: 0,
      maxValue: 35,
      description: "Marker for ovarian cancer and other gynecologic cancers.",
      synonyms: []
    },
    {
      name: "Carcinoembryonic Antigen (CEA)",
      unit: "ng/mL",
      minValue: 0,
      maxValue: 5.0,
      description: "Marker for colorectal cancer.",
      synonyms: []
    },
    {
      name: "Alpha-Fetoprotein (AFP)",
      unit: "ng/mL",
      minValue: 0,
      maxValue: 10,
      description: "Marker for liver cancer and some germ cell tumors.",
      synonyms: []
    },
    {
      name: "CA 19-9",
      unit: "U/mL",
      minValue: 0,
      maxValue: 37,
      description: "Marker for pancreatic cancer.",
      synonyms: []
    }
];
 for (const param of parameters) {
    const created = await prisma.parameter.create({
      data: {
        name: param.name,
        unit: param.unit,
        minValue: param.minValue,
        maxValue: param.maxValue,
        description: param.description,
        synonyms: {
          create: param.synonyms.map((syn) => ({
            synonym: syn,
          })),
        },
      },
    });
    console.log(`Added ${created.name}`);
  }
}

main()
  .catch((e) => {
    console.error(e);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });