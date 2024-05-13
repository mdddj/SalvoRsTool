import React, { JSX } from 'react';
import {
    Controller, useForm,
} from 'react-hook-form';

{model}

const MyDialog: React.FC<Prop> = (props) => {
    const { register, handleSubmit, reset, control} = useForm<Model>();

    //todo!提交数据
    const onFinish = async (values: Model) => {

    };

    return (
        <Dialog id={'my-dialog'} onClose={() => reset()}>
            <DialogBody>
                <DialogTitle title={'表单'} />
                <form onSubmit={handleSubmit(onFinish)}>
                    {fields}
                </form>
            </DialogBody>
            <DialogCloseBtn />
        </Dialog>
    );
};
export { MyDialog };