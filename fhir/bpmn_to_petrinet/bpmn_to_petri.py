import os
import sys
from pm4py import convert_to_petri_net
from pm4py import write_pnml
from pathlib import Path
from pm4py import read_bpmn

def convert(bpmn_path):
    if not os.path.exists(bpmn_path):
        print("Folder not found")
        sys.exit(1)
    if(os.path.isdir(bpmn_path)):
        for root, dirs, files in os.walk(bpmn_path):
            for file in files:
                if file.endswith(".bpmn"):
                    bpmn_path = str(os.path.join(root, file))
                    try:
                        bpmn = read_bpmn(bpmn_path)
                        net, im, fm = convert_to_petri_net(bpmn)
                        pnmlStr = "\\".join(os.path.dirname(bpmn_path).split("\\")[:-1])
                        pnmlPath = Path(pnmlStr)
                        pnmlPath = pnmlPath.joinpath("pnml")
                        pnmlPath = pnmlPath.joinpath(bpmn_path.split("\\")[-1])
                        write_pnml(net, im, fm, str(pnmlPath).replace(".bpmn", ".pnml"))
                        print("File converted successfully")
                    except Exception as e:
                        print("Error occurred while converting file")
                        print(e)
                        sys.exit(1)
    elif(os.path.isfile(bpmn_path)):
        if not bpmn_path.endswith(".bpmn"):
            print("Wrong File format")
            sys.exit(1)
        try:
            bpmn = read_bpmn(bpmn_path)
            net, im, fm = convert_to_petri_net(bpmn)
            pnmlStr = "\\".join(os.path.dirname(bpmn_path).split("\\")[:-1])
            pnmlPath = Path(pnmlStr)
            pnmlPath = pnmlPath.joinpath("pnml")
            pnmlPath = pnmlPath.joinpath(bpmn_path.split("\\")[-1])
            write_pnml(net, im, fm, str(pnmlPath).replace(".bpmn", ".pnml"))
            print("File converted successfully")
        except Exception as e:
            print("Error occurred while converting file")
            print(e)
            sys.exit(1)
    else:
        print("Wrong File/Path format")


if __name__ == "__main__":
    if(len(sys.argv) != 2):
        print("Usage: python pm4py_bpmn_to_petri.py <path_to_bpmn_files>")
        sys.exit(1)
    bpmn_path = sys.argv[1]
    convert(bpmn_path)