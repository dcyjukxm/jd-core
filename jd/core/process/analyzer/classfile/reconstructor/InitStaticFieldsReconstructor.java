package jd.core.process.analyzer.classfile.reconstructor;

import java.util.List;
import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.Field;
import jd.core.model.classfile.Method;
import jd.core.model.classfile.constant.ConstantFieldref;
import jd.core.model.classfile.constant.ConstantNameAndType;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.PutStatic;
import jd.core.process.analyzer.classfile.visitor.SearchInstructionByOpcodeVisitor;

public class InitStaticFieldsReconstructor {
   public static void Reconstruct(ClassFile classFile) {
      Method method = classFile.getStaticMethod();
      if(method != null) {
         Field[] fields = classFile.getFields();
         if(fields != null) {
            List list = method.getFastNodes();
            if(list != null) {
               ConstantPool constants = classFile.getConstantPool();
               int indexInstruction = 0;
               int length = list.size();

               int indexField;
               Instruction instruction;
               PutStatic putStatic;
               ConstantFieldref cfr;
               ConstantNameAndType cnat;
               int lengthBeforeSubstitution;
               Field field;
               Instruction valueref;
               for(indexField = 0; indexInstruction < length; ++indexInstruction) {
                  instruction = (Instruction)list.get(indexInstruction);
                  if(instruction.opcode != 179) {
                     break;
                  }

                  putStatic = (PutStatic)instruction;
                  cfr = constants.getConstantFieldref(putStatic.index);
                  if(cfr.class_index != classFile.getThisClassIndex()) {
                     break;
                  }

                  cnat = constants.getConstantNameAndType(cfr.name_and_type_index);
                  lengthBeforeSubstitution = list.size();

                  while(indexField < fields.length) {
                     field = fields[indexField++];
                     if((field.access_flags & 8) != 0 && cnat.descriptor_index == field.descriptor_index && cnat.name_index == field.name_index) {
                        valueref = putStatic.valueref;
                        if(SearchInstructionByOpcodeVisitor.visit((Instruction)valueref, 25) == null && SearchInstructionByOpcodeVisitor.visit((Instruction)valueref, 268) == null && SearchInstructionByOpcodeVisitor.visit((Instruction)valueref, 21) == null) {
                           field.setValueAndMethod(valueref, method);
                           if(valueref.opcode == 283) {
                              valueref.opcode = 282;
                           }

                           list.remove(indexInstruction--);
                        }
                        break;
                     }
                  }

                  if(lengthBeforeSubstitution == list.size()) {
                     break;
                  }
               }

               indexInstruction = list.size();
               if(indexInstruction > 0) {
                  --indexInstruction;
                  indexField = fields.length;

                  while(indexInstruction-- > 0) {
                     instruction = (Instruction)list.get(indexInstruction);
                     if(instruction.opcode != 179) {
                        break;
                     }

                     putStatic = (PutStatic)instruction;
                     cfr = constants.getConstantFieldref(putStatic.index);
                     if(cfr.class_index != classFile.getThisClassIndex()) {
                        break;
                     }

                     cnat = constants.getConstantNameAndType(cfr.name_and_type_index);
                     lengthBeforeSubstitution = list.size();

                     while(indexField-- > 0) {
                        field = fields[indexField];
                        if((field.access_flags & 8) != 0 && cnat.descriptor_index == field.descriptor_index && cnat.name_index == field.name_index) {
                           valueref = putStatic.valueref;
                           if(SearchInstructionByOpcodeVisitor.visit((Instruction)valueref, 25) == null && SearchInstructionByOpcodeVisitor.visit((Instruction)valueref, 268) == null && SearchInstructionByOpcodeVisitor.visit((Instruction)valueref, 21) == null) {
                              field.setValueAndMethod(valueref, method);
                              if(valueref.opcode == 283) {
                                 valueref.opcode = 282;
                              }

                              list.remove(indexInstruction);
                           }
                           break;
                        }
                     }

                     if(lengthBeforeSubstitution == list.size()) {
                        break;
                     }
                  }
               }

            }
         }
      }
   }
}
